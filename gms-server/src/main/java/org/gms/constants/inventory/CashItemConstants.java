package org.gms.constants.inventory;

import java.util.HashMap;

/***
 * 现金消耗
 */
public class CashItemConstants
{
    static public HashMap<Integer,String> m_mapCashItems = new HashMap<>();
    static public int m_CashItemsNpc = 0;

    static
    {
        m_CashItemsNpc = 1012117;
        //m_mapCashItems.put(5530000,"新手礼包");
    }
}
