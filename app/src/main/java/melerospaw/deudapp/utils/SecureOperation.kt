package melerospaw.deudapp.utils

infix fun Float.secureAdd(operand2: Float) : Float {

    return if ((this.isInfinite() && operand2.isInfinite()) ||
            this.isInfinite()) {
        this
    } else if (operand2.isInfinite()) {
        operand2
    } else {
        this + operand2
    }
}

infix fun Float.secureSubtract(operand2: Float) : Float {
    return if (this.isInfinite() && operand2.isInfinite()) {
        0F
    } else if (this.isInfinite()) {
        this
    } else if (operand2.isInfinite()) {
        -operand2
    } else {
        this - operand2
    }
}
