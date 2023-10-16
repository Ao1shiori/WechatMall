package com.mall.wxw.mq.constant;

public class MqConst {
    /**
     * 消息补偿
     */
    public static final String MQ_KEY_PREFIX = "mall.mq:list";
    public static final int RETRY_COUNT = 3;

    /**
     * 商品上下架
     */
    public static final String EXCHANGE_GOODS_DIRECT = "mall.goods.direct";
    public static final String ROUTING_GOODS_UPPER = "mall.goods.upper";
    public static final String ROUTING_GOODS_LOWER = "mall.goods.lower";
    //队列
    public static final String QUEUE_GOODS_UPPER  = "mall.goods.upper";
    public static final String QUEUE_GOODS_LOWER  = "mall.goods.lower";

    /**
     * 团长上下线
     */
    public static final String EXCHANGE_LEADER_DIRECT = "mall.leader.direct";
    public static final String ROUTING_LEADER_UPPER = "mall.leader.upper";
    public static final String ROUTING_LEADER_LOWER = "mall.leader.lower";
    //队列
    public static final String QUEUE_LEADER_UPPER  = "mall.leader.upper";
    public static final String QUEUE_LEADER_LOWER  = "mall.leader.lower";

    //订单
    public static final String EXCHANGE_ORDER_DIRECT = "mall.order.direct";
    public static final String ROUTING_ROLLBACK_STOCK = "mall.rollback.stock";
    public static final String ROUTING_MINUS_STOCK = "mall.minus.stock";

    public static final String ROUTING_DELETE_CART = "mall.delete.cart";
    //解锁普通商品库存
    public static final String QUEUE_ROLLBACK_STOCK = "mall.rollback.stock";
    public static final String QUEUE_SECKILL_ROLLBACK_STOCK = "mall.seckill.rollback.stock";
    public static final String QUEUE_MINUS_STOCK = "mall.minus.stock";
    public static final String QUEUE_DELETE_CART = "mall.delete.cart";

    //支付
    public static final String EXCHANGE_PAY_DIRECT = "mall.pay.direct";
    public static final String ROUTING_PAY_SUCCESS = "mall.pay.success";
    public static final String QUEUE_ORDER_PAY  = "mall.order.pay";
    public static final String QUEUE_LEADER_BILL  = "mall.leader.bill";

    //取消订单
    public static final String EXCHANGE_CANCEL_ORDER_DIRECT = "mall.cancel.order.direct";
    public static final String ROUTING_CANCEL_ORDER = "mall.cancel.order";
    //延迟取消订单队列
    public static final String QUEUE_CANCEL_ORDER  = "mall.cancel.order";

    /**
     * 定时任务
     */
    public static final String EXCHANGE_DIRECT_TASK = "mall.exchange.direct.task";
    public static final String ROUTING_TASK_23 = "mall.task.23";
    //队列
    public static final String QUEUE_TASK_23  = "mall.queue.task.23";
}