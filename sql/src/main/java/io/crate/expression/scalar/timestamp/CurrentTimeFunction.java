/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate.expression.scalar.timestamp;

import io.crate.data.Input;
import io.crate.expression.scalar.DateFormatFunction;
import io.crate.expression.scalar.ScalarFunctionModule;
import io.crate.expression.scalar.TimestampFormatter;
import io.crate.metadata.FunctionIdent;
import io.crate.metadata.FunctionInfo;
import io.crate.metadata.Scalar;
import io.crate.metadata.TransactionContext;
import io.crate.metadata.functions.Signature;
import io.crate.types.DataTypes;
import io.crate.types.TimestampType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Collections;
import java.util.List;

import static io.crate.types.TypeSignature.parseTypeSignature;

public class CurrentTimeFunction extends Scalar<String, Integer> {

    public static final String NAME = "current_time";

    public static final FunctionInfo INFO = new FunctionInfo(
        new FunctionIdent(NAME, List.of(DataTypes.INTEGER)),
        DataTypes.STRING,
        FunctionInfo.Type.SCALAR,
        Collections.emptySet());

    public static void register(ScalarFunctionModule module) {
        module.register(
            Signature.scalar(
                NAME,
                parseTypeSignature("integer"),
                parseTypeSignature("string")
            ),
            args -> new CurrentTimeFunction()
        );
    }

    private final CurrentTimestampFunction tsSupplier = new CurrentTimestampFunction();

    @Override
    public FunctionInfo info() {
        return INFO;
    }

    @Override
    @SafeVarargs
    public final String evaluate(TransactionContext txnCtx, Input<Integer>... args) {
        Long ts = tsSupplier.evaluate(txnCtx, args);
        return TimestampFormatter.format(
            DateFormatFunction.DEFAULT_FORMAT,
            new DateTime(
                TimestampType.INSTANCE_WITH_TZ.value(ts),
                DateTimeZone.UTC));
    }
}
