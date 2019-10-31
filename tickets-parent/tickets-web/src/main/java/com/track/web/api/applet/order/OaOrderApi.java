package com.track.web.api.applet.order;

import com.github.pagehelper.PageInfo;
import com.track.common.enums.system.ResultCode;
import com.track.core.interaction.JsonViewData;
import com.track.data.dto.applet.order.OrderSettlementDto;
import com.track.data.dto.applet.order.SearchMyOrderDto;
import com.track.data.vo.applet.order.MyOrderListVo;
import com.track.data.vo.applet.order.OrderSettlementVo;
import com.track.order.service.IOmOrderService;
import com.track.web.base.BaseWeb;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yeJH
 * @since 2019/10/29 17:17
 * <p>
 * 小程序订单接口
 */
@Api(tags = "小程序_订单接口")
@RestController
@RequestMapping("/applet/order")
@Slf4j
public class OaOrderApi extends BaseWeb {

    @Autowired
    private IOmOrderService service;


    /**
     * @Author yeJH
     * @Date 2019/10/29 16:02
     * @Description 查询我的订单列表
     *
     * @Update yeJH
     *
     * @param  searchMyOrderDto  查询条件
     * @return com.track.core.interaction.JsonViewData<com.github.pagehelper.PageInfo<com.track.data.vo.manage.order.ManageOrderListVo>>
     **/
    @ApiOperation(value = "查询我的订单列表", notes = "根据状态查询我的订单列表")
    @PostMapping("/searchMyOrder")
    public JsonViewData<PageInfo<MyOrderListVo>> searchMyOrder(
            @ApiParam(required = true, name = "searchOrderDto", value = "查询条件")
            @Validated @RequestBody SearchMyOrderDto searchMyOrderDto) {

        //PageInfo<MyOrderListVo> myOrderListVoPageInfo = service.searchMyOrder(searchMyOrderDto);
        return new JsonViewData(ResultCode.SUCCESS, "查找成功" /*,myOrderListVoPageInfo*/);

    }

    /**
     * @Author yeJH
     * @Date 2019/10/30 17:49
     * @Description 订单结算
     *
     * @Update yeJH
     *
     * @param  orderSettlementDto
     * @return com.track.core.interaction.JsonViewData<com.track.data.vo.applet.order.OrderSettlementVo>
     **/
    @ApiOperation(value = "订单结算", notes = "选择门票场次，档次，添加数量去结算")
    @PostMapping("/settlement")
    public JsonViewData<OrderSettlementVo> settlement(
            @ApiParam(required = true, name = "orderSettlementDto", value = "结算参数")
            @Validated @RequestBody OrderSettlementDto orderSettlementDto) {


        return new JsonViewData(ResultCode.SUCCESS, "查找成功",
                service.settlement(orderSettlementDto));

    }

}
