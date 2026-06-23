/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.lower

import org.jetbrains.kotlin.backend.common.lower.AddFunctionSupertypeToSuspendFunctionLowering
import org.jetbrains.kotlin.backend.common.lower.coroutines.getOrCreateFunctionWithContinuationStub
import org.jetbrains.kotlin.backend.wasm.WasmBackendContext
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.types.*

// Overrides return type transformation for stack switching coroutines compilation.
internal class WasmAddFunctionSupertypeToSuspendFunctionLowering(override val context: WasmBackendContext) :
    AddFunctionSupertypeToSuspendFunctionLowering(context) {

    protected override fun IrSimpleFunction.getLowered() = if (isSuspend)
        if (context.wasmUseStackSwitching) {
            getOrCreateFunctionWithContinuationStub(context) { it.returnType }
        } else {
            getOrCreateFunctionWithContinuationStub(context)
        }
    else
        this

    protected override fun IrType.transformReturnType() =
        if (!context.wasmUseStackSwitching) context.irBuiltIns.anyNType else this
}
