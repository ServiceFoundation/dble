package io.mycat.plan.common.item.function.timefunc;

import io.mycat.plan.common.item.Item;
import io.mycat.plan.common.item.function.ItemFunc;
import io.mycat.plan.common.item.function.primary.ItemIntFunc;
import io.mycat.plan.common.ptr.LongPtr;
import io.mycat.plan.common.time.MySQLTime;
import io.mycat.plan.common.time.MyTime;

import java.math.BigInteger;
import java.util.List;

public class ItemFuncWeek extends ItemIntFunc {

    public ItemFuncWeek(List<Item> args) {
        super(args);
    }

    @Override
    public final String funcName() {
        return "week";
    }

    @Override
    public BigInteger valInt() {
        MySQLTime ltime = new MySQLTime();
        if (getArg0Date(ltime, MyTime.TIME_NO_ZERO_DATE))
            return BigInteger.ZERO;
        return BigInteger.valueOf(MyTime.calc_week(ltime, MyTime.week_mode(args.size() > 1 ? args.get(1).valInt().intValue() : 0), new LongPtr(0)));
    }

    @Override
    public void fixLengthAndDec() {
        fixCharLength(2); /* 0..54 */
        maybeNull = true;
    }

    @Override
    public ItemFunc nativeConstruct(List<Item> realArgs) {
        return new ItemFuncWeek(realArgs);
    }
}
