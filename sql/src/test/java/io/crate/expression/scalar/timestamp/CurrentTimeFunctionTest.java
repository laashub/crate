package io.crate.expression.scalar.timestamp;

import io.crate.expression.scalar.AbstractScalarFunctionsTest;
import io.crate.expression.scalar.DateFormatFunction;
import io.crate.expression.scalar.TimestampFormatter;
import io.crate.expression.symbol.Literal;
import io.crate.types.TimestampType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;

public class CurrentTimeFunctionTest extends AbstractScalarFunctionsTest {

    private static final long EXPECTED_TIMESTAMP = 257508000000L;
    private static final String EXPECTED_TIMESTAMP_STR = "1978-02-28T10:00:00.000000Z";

    private static String format(long ts) {
        return TimestampFormatter.format(
            DateFormatFunction.DEFAULT_FORMAT,
            new DateTime(
                TimestampType.INSTANCE_WITH_TZ.value(ts),
                DateTimeZone.UTC));
    }

    @Before
    public void prepare() {
        DateTimeUtils.setCurrentMillisFixed(EXPECTED_TIMESTAMP);
    }

    @After
    public void cleanUp() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void timestampIsCreatedCorrectly() {
        assertEvaluate("current_time", EXPECTED_TIMESTAMP_STR);
        assertEvaluate("current_time", format(EXPECTED_TIMESTAMP));
    }

    @Test
    public void precisionOfZeroDropsAllFractionsOfSeconds() {
        assertEvaluate("current_time(0)", format(EXPECTED_TIMESTAMP - (EXPECTED_TIMESTAMP % 1000)));
    }

    @Test
    public void precisionOfOneDropsLastTwoDigitsOfFractionsOfSecond() {
        assertEvaluate("current_time(1)", format(EXPECTED_TIMESTAMP - (EXPECTED_TIMESTAMP % 100)));
    }

    @Test
    public void precisionOfTwoDropsLastDigitOfFractionsOfSecond() {
        assertEvaluate("current_time(2)", format(EXPECTED_TIMESTAMP - (EXPECTED_TIMESTAMP % 10)));
    }

    @Test
    public void precisionOfThreeKeepsAllFractionsOfSeconds() {
        assertEvaluate("current_time(3)", EXPECTED_TIMESTAMP_STR);
    }

    @Test
    public void precisionLargerThan3RaisesException() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Precision must be between 0 and 3");
        assertEvaluate("current_time(4)", null);
    }

    @Test
    public void integerIsNormalizedToLiteral() {
        assertNormalize("current_time(1)", instanceOf(Literal.class));
    }
}
