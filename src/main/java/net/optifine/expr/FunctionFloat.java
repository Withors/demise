package net.optifine.expr;

import net.optifine.shaders.uniform.Smoother;

import java.util.Objects;

public class FunctionFloat implements IExpressionFloat {
    private final FunctionType type;
    private final IExpression[] arguments;
    private int smoothId = -1;

    public FunctionFloat(FunctionType type, IExpression[] arguments) {
        this.type = type;
        this.arguments = arguments;
    }

    public float eval() {
        IExpression[] aiexpression = this.arguments;

        if (Objects.requireNonNull(this.type) == FunctionType.SMOOTH) {
            IExpression iexpression = aiexpression[0];

            if (!(iexpression instanceof ConstantFloat)) {
                float f = evalFloat(aiexpression, 0);
                float f1 = aiexpression.length > 1 ? evalFloat(aiexpression, 1) : 1.0F;
                float f2 = aiexpression.length > 2 ? evalFloat(aiexpression, 2) : f1;

                if (this.smoothId < 0) {
                    this.smoothId = Smoother.getNextId();
                }

                float f3 = Smoother.getSmoothValue(this.smoothId, f, f1, f2);
                return f3;
            }
        }
        return this.type.evalFloat(this.arguments);
    }

    private static float evalFloat(IExpression[] exprs, int index) {
        IExpressionFloat iexpressionfloat = (IExpressionFloat) exprs[index];
        return iexpressionfloat.eval();
    }

    public ExpressionType getExpressionType() {
        return ExpressionType.FLOAT;
    }

    public String toString() {
        return this.type + "()";
    }
}
