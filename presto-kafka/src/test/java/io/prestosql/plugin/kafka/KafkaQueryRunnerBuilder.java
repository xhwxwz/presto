/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.prestosql.plugin.kafka;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Module;
import io.airlift.log.Level;
import io.airlift.log.Logging;
import io.prestosql.testing.DistributedQueryRunner;
import io.prestosql.testing.kafka.TestingKafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.airlift.configuration.ConfigurationAwareModule.combine;
import static io.airlift.testing.Closeables.closeAllSuppress;
import static io.prestosql.plugin.kafka.KafkaPlugin.DEFAULT_EXTENSION;
import static io.prestosql.testing.TestingSession.testSessionBuilder;
import static java.util.Objects.requireNonNull;

public abstract class KafkaQueryRunnerBuilder<T extends TestingKafka>
        extends DistributedQueryRunner.Builder
{
    protected final T testingKafka;
    protected Map<String, String> extraKafkaProperties = ImmutableMap.of();
    protected Module extension = DEFAULT_EXTENSION;
    protected ImmutableList.Builder<Module> additionalModulesBuilder = ImmutableList.builder();

    public KafkaQueryRunnerBuilder(T testingKafka, String defaultSessionSchema)
    {
        super(testSessionBuilder()
                .setCatalog("kafka")
                .setSchema(defaultSessionSchema)
                .build());
        this.testingKafka = requireNonNull(testingKafka, "testingKafka is null");
    }

    public KafkaQueryRunnerBuilder setExtraKafkaProperties(Map<String, String> extraKafkaProperties)
    {
        this.extraKafkaProperties = ImmutableMap.copyOf(requireNonNull(extraKafkaProperties, "extraKafkaProperties is null"));
        return this;
    }

    public KafkaQueryRunnerBuilder setExtension(Module extension)
    {
        this.extension = requireNonNull(extension, "extension is null");
        return this;
    }

    public KafkaQueryRunnerBuilder addModules(Module... modules)
    {
        for (Module module : modules) {
            additionalModulesBuilder.add(module);
        }
        return this;
    }

    @Override
    public final DistributedQueryRunner build()
            throws Exception
    {
        Logging logging = Logging.initialize();
        logging.setLevel("org.apache.kafka", Level.WARN);

        DistributedQueryRunner queryRunner = super.build();
        try {
            testingKafka.start();
            preInit(queryRunner);
            List<Module> extensions = additionalModulesBuilder.add(extension).build();
            createKafkaQueryRunner(queryRunner, testingKafka, extraKafkaProperties, extensions);
            postInit(queryRunner);
            return queryRunner;
        }
        catch (Throwable e) {
            closeAllSuppress(e, queryRunner);
            throw e;
        }
    }

    protected void preInit(DistributedQueryRunner queryRunner) throws Exception {}

    protected void postInit(DistributedQueryRunner queryRunner) throws Exception {}

    private static DistributedQueryRunner createKafkaQueryRunner(
            DistributedQueryRunner queryRunner,
            TestingKafka testingKafka,
            Map<String, String> extraKafkaProperties,
            List<Module> extensions)
            throws Exception
    {
        KafkaPlugin kafkaPlugin = new KafkaPlugin(combine(extensions));
        queryRunner.installPlugin(kafkaPlugin);
        Map<String, String> kafkaProperties = new HashMap<>(ImmutableMap.copyOf(extraKafkaProperties));
        kafkaProperties.putIfAbsent("kafka.nodes", testingKafka.getConnectString());
        kafkaProperties.putIfAbsent("kafka.messages-per-split", "1000");
        queryRunner.createCatalog("kafka", "kafka", kafkaProperties);
        return queryRunner;
    }
}
