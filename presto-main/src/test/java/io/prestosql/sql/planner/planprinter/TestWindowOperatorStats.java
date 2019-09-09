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
package io.prestosql.sql.planner.planprinter;

import io.prestosql.operator.WindowInfo;
import org.testng.annotations.Test;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class TestWindowOperatorStats
{
    @Test
    public void testEmptyDriverInfosList()
    {
        WindowInfo info = new WindowInfo(emptyList());

        WindowOperatorStats stats = WindowOperatorStats.create(info);

        assertThat(stats.getIndexSizeStdDev()).isEqualTo(Double.NaN);
        assertThat(stats.getIndexPositionsStdDev()).isEqualTo(Double.NaN);
        assertThat(stats.getIndexCountPerDriverStdDev()).isEqualTo(Double.NaN);
        assertThat(stats.getPartitionRowsStdDev()).isEqualTo(Double.NaN);
        assertThat(stats.getRowsPerDriverStdDev()).isEqualTo(Double.NaN);
        assertThat(stats.getActiveDrivers()).isEqualTo(0);
        assertThat(stats.getTotalDrivers()).isEqualTo(0);
    }
}