package io.mycat.plan.common.item.function.strfunc;

import io.mycat.plan.common.item.Item;
import io.mycat.plan.common.item.function.ItemFunc;

import java.util.List;


public class ItemFuncLower extends ItemStrFunc {

    public ItemFuncLower(List<Item> args) {
        super(args);
    }

    @Override
    public final String funcName() {
        return "lower";
    }

    @Override
    public String valStr() {
        String orgStr = args.get(0).valStr();
        if (this.nullValue = args.get(0).isNull())
            return EMPTY;
        return orgStr.toLowerCase();
    }

    @Override
    public ItemFunc nativeConstruct(List<Item> realArgs) {
        return new ItemFuncLower(realArgs);
    }
}
