package io.mycat.plan.common.item.function.timefunc;

import io.mycat.plan.common.item.Item;
import io.mycat.plan.common.item.function.ItemFunc;
import io.mycat.plan.common.item.function.primary.ItemIntFunc;
import io.mycat.plan.common.time.MySQLTime;
import io.mycat.plan.common.time.MyTime;

import java.math.BigInteger;
import java.util.List;


public class ItemFuncDayofyear extends ItemIntFunc {

    public ItemFuncDayofyear(List<Item> args) {
        super(args);
    }

    @Override
    public final String funcName() {
        return "dayofyear";
    }

    @Override
    public BigInteger valInt() {
        MySQLTime ltime = new MySQLTime();
        if (getArg0Date(ltime, MyTime.TIME_NO_ZERO_DATE))
            return BigInteger.ZERO;
        return BigInteger.valueOf(
                MyTime.calc_daynr(ltime.year, ltime.month, ltime.day) - MyTime.calc_daynr(ltime.year, 1, 1) + 1);
    }

    @Override
    public void fixLengthAndDec() {
        maxLength = (3);
        maybeNull = true;
    }

    @Override
    public ItemFunc nativeConstruct(List<Item> realArgs) {
        return new ItemFuncDayofyear(realArgs);
    }
}
