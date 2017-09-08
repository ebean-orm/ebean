
    drop view if exists order_agg_vw;
    create view order_agg_vw as
    select d.order_id, sum(d.order_qty * d.unit_price) as order_total,
           sum(d.ship_qty * d.unit_price) as ship_total
    from o_order_detail d
    group by d.order_id;

  