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
package io.prestosql.tests.cli;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.airlift.log.Logger;
import io.prestosql.tempto.ProductTest;

import java.io.IOException;
import java.util.List;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.readLines;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;

public class PrestoCliLauncher
        extends ProductTest
{
    private static final Logger log = Logger.get(PrestoCliLauncher.class);

    protected static final long TIMEOUT = 300 * 1000; // 30 secs per test
    protected final List<String> nationTableInteractiveLines;
    protected final List<String> nationTableBatchLines;

    @Inject
    @Named("databases.presto.host")
    protected String serverHost;

    @Inject
    @Named("databases.presto.server_address")
    protected String serverAddress;

    protected PrestoCliProcess presto;

    protected PrestoCliLauncher()
            throws IOException
    {
        nationTableInteractiveLines = readLines(getResource("io/prestosql/tests/cli/interactive_query.results"), UTF_8);
        nationTableBatchLines = readLines(getResource("io/prestosql/tests/cli/batch_query.results"), UTF_8);
    }

    protected void stopPresto()
            throws InterruptedException
    {
        if (presto != null) {
            presto.close();
            presto = null;
        }
    }

    protected void launchPrestoCli(String... arguments)
            throws IOException
    {
        launchPrestoCli(asList(arguments));
    }

    protected void launchPrestoCli(List<String> arguments)
            throws IOException
    {
        presto = new PrestoCliProcess(getProcessBuilder(arguments).start());
    }

    protected ProcessBuilder getProcessBuilder(List<String> arguments)
    {
        List<String> command = ImmutableList.<String>builder()
                .add("/docker/presto-cli")
                .addAll(arguments)
                .build();

        log.info("Running command %s", command);
        return new ProcessBuilder(command);
    }
}
