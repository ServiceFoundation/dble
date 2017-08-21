package io.mycat.plan.common.item.function.mathsfunc;

import io.mycat.plan.common.item.Item;
import io.mycat.plan.common.item.function.ItemFunc;
import io.mycat.plan.common.item.function.primary.ItemDecFunc;

import java.math.BigDecimal;
import java.util.List;


public class ItemFuncSqrt extends ItemDecFunc {

    public ItemFuncSqrt(List<Item> args) {
        super(args);
    }

    @Override
    public final String funcName() {
        return "sqrt";
    }

    public BigDecimal valReal() {
        double value = args.get(0).valReal().doubleValue();

        if ((nullValue = args.get(0).nullValue))
            return BigDecimal.ZERO;
        return new BigDecimal(Math.sqrt(value));
    }

    @Override
    public ItemFunc nativeConstruct(List<Item> realArgs) {
        return new ItemFuncSqrt(realArgs);
    }
}
