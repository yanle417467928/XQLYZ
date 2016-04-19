package com.ynyes.lyz.controller.management;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ynyes.lyz.entity.TdAgencyFund;
import com.ynyes.lyz.entity.TdGathering;
import com.ynyes.lyz.entity.TdGoodsInOut;
import com.ynyes.lyz.entity.TdManager;
import com.ynyes.lyz.entity.TdManagerRole;
import com.ynyes.lyz.entity.TdReturnReport;
import com.ynyes.lyz.entity.TdSalesDetail;
import com.ynyes.lyz.entity.TdWareHouse;
import com.ynyes.lyz.service.TdAgencyFundService;
import com.ynyes.lyz.service.TdCityService;
import com.ynyes.lyz.service.TdDiySiteService;
import com.ynyes.lyz.service.TdGatheringService;
import com.ynyes.lyz.service.TdGoodsInOutService;
import com.ynyes.lyz.service.TdManagerRoleService;
import com.ynyes.lyz.service.TdManagerService;
import com.ynyes.lyz.service.TdReturnReportService;
import com.ynyes.lyz.service.TdSalesDetailService;
import com.ynyes.lyz.service.TdUserService;
import com.ynyes.lyz.service.TdWareHouseService;
import com.ynyes.lyz.util.SiteMagConstant;

@Controller
@RequestMapping("/Verwalter/statement")
public class TdManagerStatementController extends TdManagerBaseController {
	
	@Autowired
	TdGoodsInOutService tdGoodsInOutService;
	@Autowired
	TdManagerService tdManagerService;
	@Autowired
	TdManagerRoleService tdManagerRoleService;
	@Autowired
	TdWareHouseService tdWareHouseService;
	@Autowired
	TdUserService tdUserService;
	@Autowired
	TdDiySiteService tdDiySiteService;
	@Autowired
	TdCityService tdCityService;
	@Autowired
	TdAgencyFundService tdAgencyFundService;
	@Autowired
	TdGatheringService tdGatheringService;
	@Autowired
	TdSalesDetailService tdSalesDetailService;
	@Autowired
	TdReturnReportService tdReturnReportService;
	
	
    /*
	 * 报表下载
	 */
	@RequestMapping(value = "/downdata",method = RequestMethod.GET)
	@ResponseBody
	public String dowmDataGoodsInOut(HttpServletRequest req,ModelMap map,String begindata,String enddata,HttpServletResponse response,String diyCode,String cityName,Long statusId)
	{
		//检查登录
		String username = (String) req.getSession().getAttribute("manager");
		if (null == username) {
			return "redirect:/Verwalter/login";
		}
		
		//检查权限
		TdManager tdManager = tdManagerService.findByUsernameAndIsEnableTrue(username);
		TdManagerRole tdManagerRole = null;
		if (null != tdManager && null != tdManager.getRoleId())
		{
			tdManagerRole = tdManagerRoleService.findOne(tdManager.getRoleId());
		}
		if (tdManagerRole == null)
		{
			return "redirect:/Verwalter/login";
		}
		
		Date begin = stringToDate(begindata,null);
		Date end = stringToDate(enddata,null);
		//设置默认时间
		if(null==begin){
			begin=getStartTime();
		}
		if(null==end){
			end=getEndTime();
		}
		//门店管理员只能查询归属门店
		if (tdManagerRole.getTitle().equalsIgnoreCase("门店")) 
			{
	        	diyCode=tdManager.getDiyCode();
	        	cityName=null;
			}
		
        //获取到导出的excel
		HSSFWorkbook wb=acquireHSSWorkBook(statusId, begin, end, diyCode, cityName, username);

		String exportAllUrl = SiteMagConstant.backupPath;
        download(wb, exportAllUrl, response,acquireFileName(statusId));
        return "";
	}
	
	/**
	 * 报表 展示
	 * @param keywords 订单号
	 * @param page 当前页
	 * @param size 每页显示行数
	 * @param __EVENTTARGET
	 * @param __EVENTARGUMENT
	 * @param __VIEWSTATE
	 * @param map 
	 * @param req
	 * @param orderStartTime 开始时间
	 * @param orderEndTime 结束时间
	 * @param diyCode 门店编号
	 * @param cityName 城市名称
	 * @param statusid 0:出退货报表 1:代收款报表2：收款报表3：销售明细报表4：退货报表5：领用记录报表
	 * @return
	 */
	@RequestMapping(value = "/goodsInOut/list/{statusId}")
	public String goodsListDialog(String keywords,@PathVariable Long statusId, Integer page, Integer size,
			String __EVENTTARGET, String __EVENTARGUMENT, String __VIEWSTATE,
			ModelMap map, HttpServletRequest req,String orderStartTime,String orderEndTime,String diyCode,String cityName,
			String oldOrderStartTime,String oldOrderEndTime) {
		
		String username = (String) req.getSession().getAttribute("manager");
		//判断是否登陆
		if (null == username)
		{
			return "redirect:/Verwalter/login";
		}

		TdManager tdManager = tdManagerService.findByUsernameAndIsEnableTrue(username);
		TdManagerRole tdManagerRole = null;
		if (null != tdManager && null != tdManager.getRoleId())
		{
			tdManagerRole = tdManagerRoleService.findOne(tdManager.getRoleId());
		}
		//判断是否有权限
		if (tdManagerRole == null)
		{
			return "redirect:/Verwalter/login";
		}

		if (null == page || page < 0) {
			page = 0;
		}

		if (null == size || size <= 0) {
			size = SiteMagConstant.pageSize;
		}

		if (null != __EVENTTARGET) {
			if (__EVENTTARGET.equalsIgnoreCase("btnCancel"))
			{
			} 
			else if (__EVENTTARGET.equalsIgnoreCase("btnConfirm"))
			{
			}
			else if (__EVENTTARGET.equalsIgnoreCase("btnDelete"))
			{
			}
			else if (__EVENTTARGET.equalsIgnoreCase("btnPage")) 
			{
				if (null != __EVENTARGUMENT) 
				{
					page = Integer.parseInt(__EVENTARGUMENT);
				}
			}else if(__EVENTTARGET.equals("btnSearch")){
				page=0;
			}
		}
		
		Date begin=null;
		Date end=null;
		try {//字符串转换为时间
			begin=stringToDate(orderStartTime,null);
			end=stringToDate(orderEndTime,null);
		} catch (Exception e) {
			System.out.println(e);
		}
		if(begin==null){//如果为空设置为默认时间
			begin=getStartTime();
			orderStartTime=dateToString(begin,null);
		}
		if(end==null){//如果为空设置为默认时间
			end=getEndTime();
			orderEndTime=dateToString(end,null);
		}
		
		if(!orderStartTime.equals(oldOrderStartTime) || !orderEndTime.equals(oldOrderEndTime)){
			//调用存储过程查询数据
			callProcedure(statusId, __EVENTTARGET, begin, end, username);
		}
		
		
		
		//如果是门店管理员只能查询自己门店
		String diySiteCode="";
		if (tdManagerRole.getTitle().equalsIgnoreCase("门店")){
			diySiteCode=tdManager.getDiyCode();
		}else if(tdManagerRole.getIsSys()){
			diySiteCode=diyCode;
		}
		
		//根据报表类型 查询相应数据到map
		addOrderListToMap(map, statusId, keywords, begin, end, diySiteCode, cityName, username, size, page);
	
		
		//城市和门店信息
		if (tdManagerRole.getIsSys()){
			map.addAttribute("diySiteList",tdDiySiteService.findAll());
			map.addAttribute("cityList", tdCityService.findAll());
		}
		// 参数注回
		map.addAttribute("orderNumber", keywords);
		map.addAttribute("orderStartTime", orderStartTime);
		map.addAttribute("orderEndTime", orderEndTime);
		map.addAttribute("diyCode", diyCode);
		map.addAttribute("cityName", cityName);
		map.addAttribute("oldOrderStartTime", orderStartTime);
		map.addAttribute("oldOrderEndTime", orderEndTime);

		map.addAttribute("page", page);
		map.addAttribute("size", size);
		map.addAttribute("keywords", keywords);
		map.addAttribute("statusId", statusId);
		map.addAttribute("__EVENTTARGET", __EVENTTARGET);
		map.addAttribute("__EVENTARGUMENT", __EVENTARGUMENT);
		map.addAttribute("__VIEWSTATE", __VIEWSTATE);

		return "/site_mag/statement_list";
	}

	

	private String changeName(String name)
	{
//		郑州公司	11	总仓
//		天荣中转仓	1101	分仓
//		五龙口中转仓	1102	分仓
//		东大中转仓	1103	分仓
//		百姓中转仓	1104	分仓
//		主仓库	1105	分仓
		
		List<TdWareHouse> wareHouses = tdWareHouseService.findBywhNumberOrderBySortIdAsc(name);
		if (wareHouses != null && wareHouses.size() > 0)
		{
			return wareHouses.get(0).getWhName();
		}
		else 
		{
			return "未知编号：" + name;
		}
		
//		if (name == null || name.equalsIgnoreCase(""))
//		{
//			return "未知";
//		}
//		if (name.equalsIgnoreCase("11"))
//		{
//			return "郑州公司";
//		}
//		else if (name.equalsIgnoreCase("1101"))
//		{
//			return "天荣中转仓";
//		}
//		else if (name.equalsIgnoreCase("1102"))
//		{
//			return "五龙口中转仓";
//		}
//		else if (name.equalsIgnoreCase("1103"))
//		{
//			return "东大中转仓";
//		}
//		else if (name.equalsIgnoreCase("1104"))
//		{
//			return "百姓中转仓";
//		}
//		else if (name.equalsIgnoreCase("1105"))
//		{
//			return "主仓库";
//		}
//		else
//		{
//			return "未知编号：" + name;
//		}
	}
	
	/**
	 * 根据报表类型 调用相应的存储过程 插入数据
	 * @param statusId 报表类型
	 * @param __EVENTTARGET
	 * @param begin 开始时间
	 * @param end 结算时间
	 * @param username 当前用户
	 */
	private void callProcedure(Long statusId,String __EVENTTARGET,Date begin,Date end,String username){
		try {//调用存储过程 报错
			if(null != __EVENTTARGET && __EVENTTARGET.equalsIgnoreCase("btnPage")){
				return;
			}else if(statusId==0){//出退货报表
				tdGoodsInOutService.callinsertGoodsInOutInitial(begin, end,username);
			}else if(statusId==1){//代收款报表
				tdAgencyFundService.callInsertAgencyFund(begin, end,username);
			}else if(statusId==2){//收款报表
				tdGatheringService.callInsertGathering(begin, end, username);
			}else if(statusId==3){//销售明细报表
				tdSalesDetailService.callInsertSalesDetail(begin, end, username);
			}else if(statusId==4){//退货报表
				tdReturnReportService.callInsertReturnReport(begin, end, username);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	/**
	 * 根据报表类型 查询相应数据到map
	 * @param map 
	 * @param statusId 报表类型
	 * @param keywords 订单号
	 * @param begin 开始时间
	 * @param end 结算时间
	 * @param diySiteCode 门店编号
	 * @param cityName 城市名称
	 * @param username 当前用户
	 * @param size
	 * @param page
	 */
	private void addOrderListToMap(ModelMap map,Long statusId,String keywords,Date begin,Date end,String diySiteCode,String cityName,String username,int size,int page){
		if(statusId==0){//出退货报表
			map.addAttribute("order_page",tdGoodsInOutService.searchList(keywords,begin, end, diySiteCode, cityName,username, size, page));
		}else if(statusId==1){//代收款报表
			map.addAttribute("order_page",tdAgencyFundService.searchList(keywords,begin, end,cityName ,diySiteCode ,username, size, page));
		}else if(statusId==2){//收款报表
			map.addAttribute("order_page",tdGatheringService.searchList(keywords,begin, end,cityName ,diySiteCode ,username, size, page));
		}else if(statusId==3){//销售明细报表
			map.addAttribute("order_page",tdSalesDetailService.searchList(keywords,begin, end,cityName ,diySiteCode ,username, size, page));
		}else if(statusId==4){//退货报表
			map.addAttribute("order_page",tdReturnReportService.searchList(keywords,begin, end,cityName ,diySiteCode ,username, size, page));
		}

	}
	/**
	 * 根据报表类型获取报表名
	 * @param statusId
	 * @return
	 */
	private String acquireFileName(Long statusId){
		String fileName="";
		if(statusId==0){
			fileName="出退货明细报表";
		}else if(statusId==1){
			fileName= "代收款报表";
		}else if(statusId==2){
			fileName= "收款报表";
		}else if(statusId==3){
			fileName= "销售明细报表";
		}else if(statusId==4){
			fileName= "退货报表";
		}else if(statusId==5){
			fileName= "领用记录报表";
		}
		return fileName;
	}
	/**
	 * 根据报表状态 设置相应的报表
	 * @param statusId
	 * @param begin 开始时间
	 * @param end 结算时间
	 * @param diyCode 门店编号
	 * @param cityName 城市名称
	 * @param username 当前用户
	 * @return
	 */
	private HSSFWorkbook acquireHSSWorkBook(Long statusId,Date begin,Date end,String diyCode,String cityName,String username){
		HSSFWorkbook wb= new HSSFWorkbook();  
		if(statusId==0){//出退货明细报表
			wb=goodsInOutWorkBook(begin, end, diyCode, cityName, username);
		}else if(statusId==1){//代收款报表
			wb=agencyFundWorkBook(begin, end, diyCode, cityName, username);
		}else if(statusId==2){//收款报表
			wb=payWorkBook(begin, end, diyCode, cityName, username);
		}else if(statusId==3){//销售明细报表
			wb=salesDetailWorkBook(begin, end, diyCode, cityName, username);
		}else if(statusId==4){//退货报表
			wb=returnWorkBook(begin, end, diyCode, cityName, username);
		}
		return wb;
	}
	/**
	 * 出退货明细报表
	 * @param begin 开始时间
	 * @param end 结束时间
	 * @param diyCode 门店编号
	 * @param cityName 城市编号
 	 * @param username 当前用户
	 * @return
	 */
	private HSSFWorkbook goodsInOutWorkBook(Date begin,Date end,String diyCode,String cityName,String username){
		// 第一步，创建一个webbook，对应一个Excel文件 
        HSSFWorkbook wb = new HSSFWorkbook();  
        // 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet  
        HSSFSheet sheet = wb.createSheet("出退货明细报表");  
        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short  
        //列宽
        int[] widths={13,25,25,13,18,18,13,13,13,13,
        		9,13,13,20,9,9,9,13,13,13,
        		20,13};
        sheetColumnWidth(sheet,widths);
        
        // 第四步，创建单元格，并设置值表头 设置表头居中  
        HSSFCellStyle style = wb.createCellStyle();  
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
        style.setWrapText(true);


       	//设置标题
        HSSFRow row = sheet.createRow((int) 0); 
        
        String[] cellValues={"门店名称","主单号","分单号","订单状态","订单日期","销售日期","客户名称","客户电话","品牌","商品类别",
				"导购","配送方式","产品编号","产品名称","数量","单价","总价","中转仓","配送人员","配送人员电话",
				"地址","客户备注"};
		cellDates(cellValues, style, row);
 
        // 第五步，设置值  
        List<TdGoodsInOut> goodsInOutList=tdGoodsInOutService.searchGoodsInOut(begin, end, cityName, diyCode,username);
        
        
        Integer i = 0;
        for (TdGoodsInOut goodsInOut : goodsInOutList)
        {
        	row = sheet.createRow((int) i + 1);
        	if (null != goodsInOut.getDiySiteName())
        	{//门店名称
            	row.createCell(0).setCellValue(goodsInOut.getDiySiteName());
    		}
        	if (null != goodsInOut.getMainOrderNumber())
        	{//主单号
            	row.createCell(1).setCellValue(goodsInOut.getMainOrderNumber());
    		}
        	if (null != goodsInOut.getOrderNumber())
        	{//分单号
            	row.createCell(2).setCellValue(goodsInOut.getOrderNumber());
    		}
        	if (null != goodsInOut.getStatusId())
        	{//订单状态
        		String statusStr = orderStatus(goodsInOut.getStatusId());
            	row.createCell(3).setCellValue(statusStr);
    		}
        	if (null != goodsInOut.getOrderTime())
        	{//订单日期
        		Date orderTime = goodsInOut.getOrderTime();
        		String orderTimeStr = orderTime.toString();
            	row.createCell(4).setCellValue(orderTimeStr);
    		}
        	if (null != goodsInOut.getSalesTime())
        	{//销售日期
        		Date salesTime = goodsInOut.getSalesTime();
        		String salesTimeStr = salesTime.toString();
            	row.createCell(5).setCellValue(salesTimeStr);
    		}
        	if (null != goodsInOut.getRealName())
        	{//客户名称
            	row.createCell(6).setCellValue(goodsInOut.getRealName());
    		}
        	if (null != goodsInOut.getUsername())
        	{//客户电话
            	row.createCell(7).setCellValue(goodsInOut.getUsername());
    		}
        	if (null != goodsInOut.getBrandTitle())
        	{//品牌
            	row.createCell(8).setCellValue(goodsInOut.getBrandTitle());
    		}else{
    			String brand= goodsInOut.getOrderNumber().substring(0, 2);
    			if(brand.equals("HR")){
    				row.createCell(8).setCellValue("华润");
    			}else if(brand.equals("LY")){
    				row.createCell(8).setCellValue("乐易装");
    			}else if(brand.equals("YR")){
    				row.createCell(8).setCellValue("莹润");
    			}else{
    				row.createCell(8).setCellValue("其他");
    			}
    		}
        	if (null != goodsInOut.getCategoryTitle())
        	{//商品类别
            	row.createCell(9).setCellValue(goodsInOut.getCategoryTitle());
    		}
        	if (null != goodsInOut.getSellerRealName())
        	{//导购
            	row.createCell(10).setCellValue(goodsInOut.getSellerRealName());
    		}
        	if (null != goodsInOut.getDeliverTypeTitle())
        	{//配送方式
            	row.createCell(11).setCellValue(goodsInOut.getDeliverTypeTitle());
    		}
        	if (null != goodsInOut.getSku())
        	{//产品编号
            	row.createCell(12).setCellValue(goodsInOut.getSku());
    		}
        	if (null != goodsInOut.getGoodsTitle())
        	{//产品名称
            	row.createCell(13).setCellValue(goodsInOut.getGoodsTitle());
    		}
        	if (null != goodsInOut.getQuantity())
        	{//数量
            	row.createCell(14).setCellValue(goodsInOut.getQuantity());
    		}
        	if (null != goodsInOut.getPrice())
        	{//单价
            	row.createCell(15).setCellValue(goodsInOut.getPrice());
    		}
        	if (null != goodsInOut.getTotalPrice())
        	{//总价
            	row.createCell(16).setCellValue(goodsInOut.getTotalPrice());
    		}
        	if (null != goodsInOut.getWhNo())
        	{//中转仓
            	row.createCell(17).setCellValue(changeName(goodsInOut.getWhNo()));
    		}
        	if (null != goodsInOut.getDeliverRealName())
        	{//配送人员
            	row.createCell(18).setCellValue(goodsInOut.getDeliverRealName());
    		}
        	if (null != goodsInOut.getDeliverUsername())
        	{//配送人员电话
            	row.createCell(19).setCellValue(goodsInOut.getDeliverUsername());
    		}
        	if (null != goodsInOut.getShippingAddress())
        	{//地址
            	row.createCell(20).setCellValue(goodsInOut.getShippingAddress());
    		}
        	if (null != goodsInOut.getRemarkInfo())
        	{//客户备份
            	row.createCell(21).setCellValue(goodsInOut.getRemarkInfo());
    		}

        	i++;
		}
        return wb;
	}
	
	/**
	 * 代收款报表
	 * @param begin 开始时间
	 * @param end 结束时间
	 * @param diyCode 门店编号
	 * @param cityName 城市编号
 	 * @param username 当前用户
	 * @return
	 */
	private HSSFWorkbook agencyFundWorkBook(Date begin,Date end,String diyCode,String cityName,String username){
		// 第一步，创建一个webbook，对应一个Excel文件 
        HSSFWorkbook wb = new HSSFWorkbook();  
        // 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet  
        HSSFSheet sheet = wb.createSheet("代收款报表");  
        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short  
        //列宽
        sheet.setColumnWidth(0 , 8*256);
        sheet.setColumnWidth(1 , 13*256);
        sheet.setColumnWidth(2 , 25*256);
//      sheet.setColumnWidth(3 , 25*256);
        sheet.setColumnWidth(3 , 18*256);
        sheet.setColumnWidth(4 , 11*256);
        sheet.setColumnWidth(5 , 13*256);
        sheet.setColumnWidth(6 , 11*256);
        sheet.setColumnWidth(7 , 19*256);
        sheet.setColumnWidth(8 , 12*256);
        sheet.setColumnWidth(9 , 9*256);
        sheet.setColumnWidth(10 , 13*256);
        sheet.setColumnWidth(11 , 13*256);
        sheet.setColumnWidth(12 , 13*256);
        sheet.setColumnWidth(13 , 40*256);
        sheet.setColumnWidth(14 , 40*256);
        
        // 第四步，创建单元格，并设置值表头 设置表头居中  
        HSSFCellStyle style = wb.createCellStyle();  
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
        style.setWrapText(true);
    	//门店、门店电话、单据、日期、预存款使用金额、代收款金额、实际代收款金额、欠款、配送人员、配送人电话、收货人、收货人电话、备注信息
        HSSFRow row = sheet.createRow((int) 0); 
        HSSFCell cell = row.createCell(0);  
        cell.setCellValue("门店名称");
        cell.setCellStyle(style);
        cell = row.createCell(1);
        cell.setCellValue("门店电话");  
        cell.setCellStyle(style);  
        cell = row.createCell(2);  
        cell.setCellValue("主单号");  
        cell.setCellStyle(style);
//        cell = row.createCell(3);  
//        cell.setCellValue("分单号");  
//        cell.setCellStyle(style);
        cell = row.createCell(3);  
        cell.setCellValue("订单日期");
        cell.setCellStyle(style);
        cell = row.createCell(4);  
        cell.setCellValue("使用可提现金额");
        cell.setCellStyle(style);
        cell = row.createCell(5);  
        cell.setCellValue("使用不可提现金额");
        cell.setCellStyle(style);
        cell = row.createCell(6);  
        cell.setCellValue("代收款金额");
        cell.setCellStyle(style);
        cell = row.createCell(7);  
        cell.setCellValue("实际代收款金额");
        cell.setCellStyle(style);
        cell = row.createCell(8);  
        cell.setCellValue("欠款");
        cell.setCellStyle(style);
        cell = row.createCell(9);  
        cell.setCellValue("配送人员");
        cell.setCellStyle(style);
        cell = row.createCell(10);  
        cell.setCellValue("配送人电话");
        cell.setCellStyle(style);
        cell = row.createCell(11);  
        cell.setCellValue("收货人");
        cell.setCellStyle(style);
        cell = row.createCell(12);  
        cell.setCellValue("收货人电话");
        cell.setCellStyle(style);
        cell = row.createCell(13);  
        cell.setCellValue("收货人地址");
        cell.setCellStyle(style);
        cell = row.createCell(14);  
        cell.setCellValue("备注信息");
        cell.setCellStyle(style);
        cell = row.createCell(15);
        cell.setCellValue("现金券额度");
        cell.setCellStyle(style);
        cell = row.createCell(16);
        cell.setCellValue("订单状态");
        cell.setCellStyle(style);
        cell = row.createCell(17);
        cell.setCellValue("仓库名称");
        cell.setCellStyle(style);
        cell = row.createCell(18);
        cell.setCellValue("订单总金额");
        cell.setCellStyle(style);
        cell = row.createCell(19);
        cell.setCellValue("预约配送时间");
        cell.setCellStyle(style);
        cell = row.createCell(20);
        cell.setCellValue("实际配送时间");
        cell.setCellStyle(style);
        cell = row.createCell(21);
        cell.setCellValue("配送方式");
        cell.setCellStyle(style);
        
        List<TdAgencyFund> agencyFundList = tdAgencyFundService.searchAgencyFund(begin, end, cityName, diyCode,username);
        
        Integer i = 0;
        String mainOrderNumber=""; //主单号
        Double cashBalanceUsed=0.0;
        Double unCashBalanceUsed=0.0;
        for (TdAgencyFund agencyFund : agencyFundList)
        {
        	if("门店自提".equals(agencyFund.getDeliverTypeTitle())){//不显示门店自提的订单
        		continue; 
        	}
        	//主单号和上一张订单的主单号相同 判断为同一张订单 跳过
        	if(mainOrderNumber != null && mainOrderNumber.equals(agencyFund.getMainOrderNumber())){
        		if (null != agencyFund.getCashBalanceUsed()) //累计使用预存款
            	{
        			cashBalanceUsed+=agencyFund.getCashBalanceUsed();
                	row.createCell(4).setCellValue(cashBalanceUsed);
                	
        		}
            	if (null != agencyFund.getUnCashBalanceUsed())
            	{
            		unCashBalanceUsed+=agencyFund.getUnCashBalanceUsed();
                	row.createCell(5).setCellValue(unCashBalanceUsed);
                	
        		}
        		continue; 
        	}else{//清空累计预存款
        		 cashBalanceUsed=0.0;
        	     unCashBalanceUsed=0.0;
        	}
        	
        	row = sheet.createRow((int) i + 1);
        	
        	if (null != agencyFund.getDiySiteName())
        	{
            	row.createCell(0).setCellValue(agencyFund.getDiySiteName());
    		}
        	if (null != agencyFund.getDiySitePhone())
        	{
            	row.createCell(1).setCellValue(agencyFund.getDiySitePhone());
    		}
        	if (null != agencyFund.getMainOrderNumber())
        	{
            	row.createCell(2).setCellValue(agencyFund.getMainOrderNumber());
    		}
//        	if (null != tdOrder.getOrderNumber())
//        	{
//            	row.createCell(3).setCellValue(tdOrder.getOrderNumber());
//    		}
        	if (null != agencyFund.getOrderTime())
        	{
        		Date orderTime = agencyFund.getOrderTime();
        		String orderTimeStr = orderTime.toString();
            	row.createCell(3).setCellValue(orderTimeStr);
    		}
        	if (null != agencyFund.getCashBalanceUsed())
        	{
            	row.createCell(4).setCellValue(agencyFund.getCashBalanceUsed());
            	cashBalanceUsed=agencyFund.getCashBalanceUsed();
    		}
        	if (null != agencyFund.getUnCashBalanceUsed())
        	{
            	row.createCell(5).setCellValue(agencyFund.getUnCashBalanceUsed());
            	unCashBalanceUsed=agencyFund.getUnCashBalanceUsed();
    		}
        	
        	if (null != agencyFund.getPayPrice())
        	{
    			row.createCell(6).setCellValue(agencyFund.getPayPrice());
    		}
        	if (null != agencyFund.getPayed())
        	{
            	row.createCell(7).setCellValue(agencyFund.getPayed());
    		}
        	if (null != agencyFund.getOwned())
        	{
            	row.createCell(8).setCellValue(agencyFund.getOwned());
    		}
        	
        	if (null != agencyFund.getWhNo())
        	{
        		row.createCell(17).setCellValue(changeName(agencyFund.getWhNo()));
			}
        	if (null != agencyFund.getRealName())
			{
        		row.createCell(9).setCellValue(agencyFund.getRealName());
			}
        	if (null != agencyFund.getUsername())
        	{
            	row.createCell(10).setCellValue(agencyFund.getUsername());
    		}
        	if(null != agencyFund.getDeliverTypeTitle() && !"门店自提".equals(agencyFund.getDeliverTypeTitle())){
        		if (null != agencyFund.getShippingName())
            	{
                	row.createCell(11).setCellValue(agencyFund.getShippingName());
        		}
            	if (null != agencyFund.getShippingPhone())
            	{
                	row.createCell(12).setCellValue(agencyFund.getShippingPhone());
        		}
            	if (null != agencyFund.getShippingAddress())
            	{
                	row.createCell(13).setCellValue(agencyFund.getShippingAddress());
        		}
        	}
        	if (null != agencyFund.getRemark())
        	{
            	row.createCell(14).setCellValue(agencyFund.getRemark());
    		}
        	if (null != agencyFund.getCashCoupon())
        	{
				row.createCell(15).setCellValue(agencyFund.getCashCoupon());
			}
        	if (null != agencyFund.getStatusId())
        	{
        		String statusStr = orderStatus(agencyFund.getStatusId());
				row.createCell(16).setCellValue(statusStr);
			}
        	if (null != agencyFund.getTotalPrice())
        	{
				row.createCell(18).setCellValue(agencyFund.getTotalPrice());
			}
        	if (null != agencyFund.getDeliveryDate()) 
        	{
        		String dayTime = agencyFund.getDeliveryDate();
    			dayTime = dayTime + " " + agencyFund.getDeliveryDetailId() + ":30";
				row.createCell(19).setCellValue(dayTime);
			}
        	if (null != agencyFund.getDeliveryTime()) 
        	{
				row.createCell(20).setCellValue(agencyFund.getDeliveryTime().toString());
			}
        	if(null != agencyFund.getDeliverTypeTitle()){
        		row.createCell(21).setCellValue(agencyFund.getDeliverTypeTitle());
        	}
        	mainOrderNumber=agencyFund.getMainOrderNumber();
        	i++;
		}
        return wb;
	}
	
	/**
	 * 收款报表
	 * @param begin 开始时间
	 * @param end 结束时间
	 * @param diyCode 门店编号
	 * @param cityName 城市编号
 	 * @param username 当前用户

	 */
	private HSSFWorkbook payWorkBook(Date begin,Date end,String diyCode,String cityName,String username){
		// 第一步，创建一个webbook，对应一个Excel文件 
        HSSFWorkbook wb = new HSSFWorkbook();  
        // 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet  
        HSSFSheet sheet = wb.createSheet("收款报表");  
        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short  
        //列宽
        sheet.setColumnWidth(0 , 8*256);
        sheet.setColumnWidth(1 , 13*256);
        sheet.setColumnWidth(2 , 25*256);
        sheet.setColumnWidth(3 , 25*256);
        sheet.setColumnWidth(4 , 13*256);
        sheet.setColumnWidth(5 , 13*256);
        sheet.setColumnWidth(6 , 13*256);
        sheet.setColumnWidth(7 , 13*256);
        sheet.setColumnWidth(8 , 13*256);
        sheet.setColumnWidth(9 , 13*256);
        sheet.setColumnWidth(10 , 13*256);
        sheet.setColumnWidth(11 , 13*256);
        sheet.setColumnWidth(12 , 13*256);
        sheet.setColumnWidth(13 , 13*256);
        sheet.setColumnWidth(14 , 13*256);
        
        // 第四步，创建单元格，并设置值表头 设置表头居中  
        HSSFCellStyle style = wb.createCellStyle();  
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
        style.setWrapText(true);
    	//门店、门店电话、单据、日期、预存款使用金额、代收款金额、实际代收款金额、欠款、配送人员、配送人电话、收货人、收货人电话、备注信息
        HSSFRow row = sheet.createRow((int) 0); 
        HSSFCell cell = row.createCell(0);  
        cell.setCellValue("门店名称");
        cell.setCellStyle(style);
        cell = row.createCell(1);
        cell.setCellValue("门店电话");  
        cell.setCellStyle(style);  
        cell = row.createCell(2);  
        cell.setCellValue("主单号");  
        cell.setCellStyle(style);
        cell = row.createCell(3);  
        cell.setCellValue("分单号");  
        cell.setCellStyle(style);
        cell = row.createCell(4);  
        cell.setCellValue("品牌");  
        cell.setCellStyle(style);
        cell = row.createCell(5);
        cell.setCellValue("导购");  
        cell.setCellStyle(style);
        cell = row.createCell(6);  
        cell.setCellValue("订单日期");
        cell.setCellStyle(style);
        cell = row.createCell(7);  
        cell.setCellValue("配送方式");
        cell.setCellStyle(style);
        cell = row.createCell(8);  
        cell.setCellValue("使用可提现金额");
        cell.setCellStyle(style);
        cell = row.createCell(9);  
        cell.setCellValue("使用不可提现金额");
        cell.setCellStyle(style);
        cell = row.createCell(10);
        cell.setCellValue("使用现金劵额度");  
        cell.setCellStyle(style);
        cell = row.createCell(11); 
        cell.setCellValue("使用产品券情况");  
        cell.setCellStyle(style);
        cell = row.createCell(12); 
        cell.setCellValue("代收款金额");
        cell.setCellStyle(style);
        cell = row.createCell(13);  
        cell.setCellValue("实际代收款金额");
        cell.setCellStyle(style);
        cell = row.createCell(14);  
        cell.setCellValue("支付方式");
        cell.setCellStyle(style);
        cell = row.createCell(15);
        cell.setCellValue("第三方支付金额");
        cell.setCellStyle(style);
        cell = row.createCell(16);
        cell.setCellValue("欠款");
        cell.setCellStyle(style);
        cell = row.createCell(17);  
        cell.setCellValue("配送人员");
        cell.setCellStyle(style);
        cell = row.createCell(18);  
        cell.setCellValue("配送人电话");
        cell.setCellStyle(style);
        cell = row.createCell(19);  
        cell.setCellValue("收货人");
        cell.setCellStyle(style);
        cell = row.createCell(20);  
        cell.setCellValue("收货人电话");
        cell.setCellStyle(style);
        cell = row.createCell(21);  
        cell.setCellValue("收货人地址");
        cell.setCellStyle(style);
        cell = row.createCell(22);  
        cell.setCellValue("备注信息");
        cell.setCellStyle(style);
        cell = row.createCell(23);
        cell.setCellValue("订单状态");
        cell.setCellStyle(style);
        cell = row.createCell(24);
        cell.setCellValue("仓库名称");
        cell.setCellStyle(style);
        cell = row.createCell(25);
        cell.setCellValue("订单总金额");
        cell.setCellStyle(style);
        cell = row.createCell(26);
        cell.setCellValue("预约配送时间");
        cell.setCellStyle(style);
        cell = row.createCell(27);
        cell.setCellValue("实际配送时间");
        cell.setCellStyle(style);
        
        // 第五步，设置值  
       
        List<TdGathering> gatheringList = tdGatheringService.searchGathering(begin, end, cityName, diyCode, username);
        
        Integer i = 0;
        for (TdGathering gathering : gatheringList)
        {
        	row = sheet.createRow((int) i + 1);
        	if (null != gathering.getDiySiteName())
        	{//门店名称
            	row.createCell(0).setCellValue(gathering.getDiySiteName());
    		}
        	if (null != gathering.getDiySitePhone())
        	{//门店电话
            	row.createCell(1).setCellValue(gathering.getDiySitePhone());
    		}
        	if (null != gathering.getMainOrderNumber())
        	{//主单号
            	row.createCell(2).setCellValue(gathering.getMainOrderNumber());
    		}
        	if (null != gathering.getOrderNumber())
        	{//分单号
            	row.createCell(3).setCellValue(gathering.getOrderNumber());
    		}
        	if (null != gathering.getBrandTitle())
        	{//品牌
            	row.createCell(4).setCellValue(gathering.getBrandTitle());
    		}else{
    			String brand= gathering.getOrderNumber().substring(0, 2);
    			if(brand.equals("HR")){
    				row.createCell(4).setCellValue("华润");
    			}else if(brand.equals("LY")){
    				row.createCell(4).setCellValue("乐易装");
    			}else if(brand.equals("YR")){
    				row.createCell(4).setCellValue("莹润");
    			}else{
    				row.createCell(4).setCellValue("其他");
    			}
    		}
        	if (null != gathering.getSellerRealName())
        	{//导购
            	row.createCell(5).setCellValue(gathering.getSellerRealName());
    		}
        	if (null != gathering.getOrderTime())
        	{//订单日期
        		Date orderTime = gathering.getOrderTime();
        		String orderTimeStr = orderTime.toString();
            	row.createCell(6).setCellValue(orderTimeStr);
    		}
        	if (null != gathering.getDeliverTypeTitle())
        	{//配送方式
            	row.createCell(7).setCellValue(gathering.getDeliverTypeTitle());
    		}
        	if (null != gathering.getCashBalanceUsed())
        	{//使用可提现金额
            	row.createCell(8).setCellValue(gathering.getCashBalanceUsed());
    		}
        	if (null != gathering.getUnCashBalanceUsed())
        	{//使用不可提现金额
            	row.createCell(9).setCellValue(gathering.getUnCashBalanceUsed());
    		}
        	if (null != gathering.getCashCoupon())
        	{//使用现金劵金额
            	row.createCell(10).setCellValue(gathering.getCashCoupon());
    		}
        	if (null != gathering.getProductCoupon())
        	{//使用产品劵情况
            	row.createCell(11).setCellValue(gathering.getProductCoupon());
    		}
        	if (null != gathering.getTotalPrice())
        	{//代收款金额
            	row.createCell(12).setCellValue(gathering.getTotalPrice()-(gathering.getUnCashBalanceUsed()==null?0:gathering.getUnCashBalanceUsed())-(gathering.getCashBalanceUsed()==null?0:gathering.getCashBalanceUsed()));
    		}
        	if(null!= gathering.getPayed()){//实际代收款金额 
        			row.createCell(13).setCellValue(gathering.getPayed());
        			
        	}
        	if(null!= gathering.getOwned()){//欠款
        		row.createCell(16).setCellValue(gathering.getOwned());
        	}
        	
        	if (null != gathering.getPayTypeTitle())
        	{//支付方式
            	row.createCell(14).setCellValue(gathering.getPayTypeTitle());
    		}
        	if (null != gathering.getOtherPay())
        	{//第三方支付金额
            	row.createCell(15).setCellValue(gathering.getOtherPay());
    		}
        	if(null!= gathering.getWhNo()){//配送仓库
        		row.createCell(24).setCellValue(changeName(gathering.getWhNo()));
        	}
        	if(gathering.getRealName() != null){ //配送人员姓名 和电话
        		row.createCell(17).setCellValue(gathering.getRealName());
        	}
        	if(gathering.getUsername() != null){ //配送人员姓名 和电话
        		row.createCell(18).setCellValue(gathering.getUsername());
        	}
        	if (!"门店自提".equals(gathering.getDeliverTypeTitle()))
        	{
        		if (null != gathering.getShippingName())
            	{//收货人姓名
                	row.createCell(19).setCellValue(gathering.getShippingName());
        		}
            	if (null != gathering.getShippingPhone())
            	{//收获人电话
                	row.createCell(20).setCellValue(gathering.getShippingPhone());
        		}
            	if (null != gathering.getShippingAddress())
            	{//收货人地址
                	row.createCell(21).setCellValue(gathering.getShippingAddress());
        		}
    		}
        	if (null != gathering.getRemark())
        	{//备注信息
            	row.createCell(22).setCellValue(gathering.getRemark());
    		}
        	if (null != gathering.getStatusId())
        	{//订单状态
        		String statusStr = orderStatus(gathering.getStatusId());
				row.createCell(23).setCellValue(statusStr);
			}
        	if (null != gathering.getTotalPrice())
        	{//订单总金额
				row.createCell(25).setCellValue(gathering.getTotalGoodsPrice());
			}
        	if (null != gathering.getDeliveryDate()) 
        	{//预约配送时间
        		String dayTime = gathering.getDeliveryDate();
    			dayTime = dayTime + " " + gathering.getDeliveryDetailId() + ":30";
				row.createCell(26).setCellValue(dayTime);
			}
        	if (null != gathering.getDeliveryTime()) 
        	{//实际配送时间
				row.createCell(27).setCellValue(gathering.getDeliveryTime().toString());
			}
        	
        	i++;
		}
        return wb;
	}
	/**
	 * 销售细报表
	 * @param begin 开始时间
	 * @param end 结束时间
	 * @param diyCode 门店编号
	 * @param cityName 城市编号
 	 * @param username 当前用户
	 * @return
	 */
	private HSSFWorkbook salesDetailWorkBook(Date begin,Date end,String diyCode,String cityName,String username){
		// 第一步，创建一个webbook，对应一个Excel文件 
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFCellStyle style = workbook.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setWrapText(true);
		
		HSSFSheet sheet = workbook.createSheet("销售明细表");
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = row.createCell(0);
		cell.setCellValue("门店名称");
		cell.setCellStyle(style);
		cell = row.createCell(1);
		cell.setCellValue("主单号");
		cell.setCellStyle(style);
		cell = row.createCell(2);
		cell.setCellValue("分单号");
		cell.setCellStyle(style);
		cell = row.createCell(3);
		cell.setCellValue("下单时间");
		cell.setCellStyle(style);
		cell = row.createCell(4);
		cell.setCellValue("订单状态");
		cell.setCellStyle(style);
		cell = row.createCell(5);
		cell.setCellValue("会员电话");
		cell.setCellStyle(style);
		cell = row.createCell(6);
		cell.setCellValue("客户名称");
		cell.setCellStyle(style);
		cell = row.createCell(7);
		cell.setCellValue("产品编号");
		cell.setCellStyle(style);
		cell = row.createCell(8);
		cell.setCellValue("产品名称");
		cell.setCellStyle(style);
		cell = row.createCell(9);
		cell.setCellValue("数量");
		cell.setCellStyle(style);
		cell = row.createCell(10);
		cell.setCellValue("单价");
		cell.setCellStyle(style);
		cell = row.createCell(11);
		cell.setCellValue("总价");
		cell.setCellStyle(style);
		cell = row.createCell(12);
		cell.setCellValue("使用可提现金额");
		cell.setCellStyle(style);
		cell = row.createCell(13);
		cell.setCellValue("使用不可提现金额");
		cell.setCellStyle(style);
		cell = row.createCell(14);
		cell.setCellValue("备注");
		cell.setCellStyle(style);
		cell = row.createCell(15);
		cell.setCellValue("中转仓");
		cell.setCellStyle(style);
		cell = row.createCell(16);
		cell.setCellValue("配送人员");
		cell.setCellStyle(style);
		cell = row.createCell(17);
		cell.setCellValue("配送人员电话");
		cell.setCellStyle(style);
		cell = row.createCell(18);
		cell.setCellValue("导购姓名");
		cell.setCellStyle(style);
		cell = row.createCell(19);
		cell.setCellValue("商品类型");
		cell.setCellStyle(style);
		cell = row.createCell(20);
		cell.setCellValue("配送方式");
		cell.setCellStyle(style);
		cell = row.createCell(21);
		cell.setCellValue("收货人地址");
		cell.setCellStyle(style);
		
		List<TdSalesDetail> salesDetailList = tdSalesDetailService.searchSalesDetail(begin, end, cityName, diyCode, username);
		
		if (salesDetailList != null)
		{
			Integer i = 1;
			for (TdSalesDetail salesDetail : salesDetailList) {
				
						row = sheet.createRow(i);
						if (salesDetail.getDiySiteName() != null)
						{
							row.createCell(0).setCellValue(salesDetail.getDiySiteName());
						}
						//代付款订单没有主单号  分单号显示到主单号位置
						if(salesDetail.getStatusId() != null && salesDetail.getStatusId().equals(2L)){
							if (salesDetail.getOrderNumber() != null){
								row.createCell(1).setCellValue(salesDetail.getOrderNumber());
							}
						}else{
							if (salesDetail.getMainOrderNumber() != null)
							{
								row.createCell(1).setCellValue(salesDetail.getMainOrderNumber());
							}
							if (salesDetail.getOrderNumber() != null)
							{
								row.createCell(2).setCellValue(salesDetail.getOrderNumber());
							}
						}
						
						if (salesDetail.getOrderTime() != null)
						{
							row.createCell(3).setCellValue(salesDetail.getOrderTime().toString());
						}
						if (salesDetail.getStatusId() != null)
						{
							row.createCell(4).setCellValue(orderStatus(salesDetail.getStatusId()));
						}
						if (salesDetail.getUsername() != null)
						{
							row.createCell(5).setCellValue(salesDetail.getUsername());
						}
						if (salesDetail.getShippingName() != null)
						{
							row.createCell(6).setCellValue(salesDetail.getShippingName());
						}
						if (salesDetail.getSku() != null)
						{
							row.createCell(7).setCellValue(salesDetail.getSku());
						}
						if (salesDetail.getGoodsTitle() != null)
						{
							row.createCell(8).setCellValue(salesDetail.getGoodsTitle());
						}
						if (salesDetail.getQuantity() != null)
						{
							row.createCell(9).setCellValue(salesDetail.getQuantity());
						}
						if (salesDetail.getPrice() != null)
						{
							row.createCell(10).setCellValue(salesDetail.getPrice());
						}
						if(salesDetail.getQuantity() != null && salesDetail.getPrice() != null){
							row.createCell(11).setCellValue(salesDetail.getPrice()*salesDetail.getQuantity());
						}
						if (null != salesDetail.getCashBalanceUsed())
			        	{
			            	row.createCell(12).setCellValue(salesDetail.getCashBalanceUsed());
			    		}
			        	if (null != salesDetail.getUnCashBalanceUsed())
			        	{
			            	row.createCell(13).setCellValue(salesDetail.getUnCashBalanceUsed());
			    		}
						if (salesDetail.getRemark() != null)
						{
							row.createCell(14).setCellValue(salesDetail.getRemark());
						}
						
						
			        	if (salesDetail.getWhNo() != null )
			        	{
			        		row.createCell(15).setCellValue(changeName(salesDetail.getWhNo()));
						}
			        	if (null != salesDetail.getDeliverRealName())
						{
			        		row.createCell(16).setCellValue(salesDetail.getDeliverRealName());
						}
			        	if (null != salesDetail.getDeliverUsername())
			        	{
			            	row.createCell(17).setCellValue(salesDetail.getDeliverUsername());
			    		}
			        	if(salesDetail.getSellerRealName() != null){
			        		row.createCell(18).setCellValue(salesDetail.getSellerRealName());
			        	}
			        	if(salesDetail.getTitle() != null){
			        		row.createCell(19).setCellValue(salesDetail.getTitle());
			        	}
			        	if(salesDetail.getDeliverTypeTitle()!=null){
			        		row.createCell(20).setCellValue(salesDetail.getDeliverTypeTitle());
			        	}
			        	if(salesDetail.getShippingAddress()!=null && !"门店自提".equals(salesDetail.getDeliverTypeTitle())){
			        		row.createCell(21).setCellValue(salesDetail.getShippingAddress());
			        	}
						
						i++;
					}
		}
        return workbook;
	}
	/**
	 * 退货报表
	 * @param begin 开始时间
	 * @param end 结束时间
	 * @param diyCode 门店编号
	 * @param cityName 城市编号
 	 * @param username 当前用户
	 * @return
	 */
	private HSSFWorkbook returnWorkBook(Date begin,Date end,String diyCode,String cityName,String username){
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFCellStyle style = workbook.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setWrapText(true);

		HSSFSheet sheet = workbook.createSheet("退货报表");
		//列宽
		sheet.setColumnWidth(0 , 10*256);//退货门店
		sheet.setColumnWidth(1 , 25*256);//原订单号
		sheet.setColumnWidth(2 , 20*256);//退货单号
		sheet.setColumnWidth(3 , 13*256);//退货单状态
		sheet.setColumnWidth(4 , 13*256);//品牌
		sheet.setColumnWidth(5 , 13*256);//商品类别
		sheet.setColumnWidth(6 , 13*256);//导购
		sheet.setColumnWidth(7 , 13*256);//订单日期
		sheet.setColumnWidth(8 , 13*256);//退货日期
		sheet.setColumnWidth(9 , 13*256);//客户名称
		sheet.setColumnWidth(10 , 13*256);//客户电话
		sheet.setColumnWidth(11 , 13*256);//产品编号
		sheet.setColumnWidth(12 , 26*256);//产品名称
		sheet.setColumnWidth(13 , 13*256);//退货数量
		sheet.setColumnWidth(14 , 13*256);//退货单价
		sheet.setColumnWidth(15 , 13*256);//退货金额
		//        sheet.setColumnWidth(16 , 13*256);//退现金卷金额
		//        sheet.setColumnWidth(17 , 13*256);//退产品卷金额
		sheet.setColumnWidth(16 , 13*256);//客户备注
		sheet.setColumnWidth(17 , 13*256);//中转仓
		sheet.setColumnWidth(18 , 13*256);//配送人员
		sheet.setColumnWidth(19 , 13*256);//配送人电话
		sheet.setColumnWidth(20 , 13*256);//退货地址

		HSSFRow row = sheet.createRow(0);
		String[] cellValues={"退货门店","原订单号","退货单号","退货单状态","品牌","商品类别","导购","订单日期","退货日期","客户名称",
				"客户电话","产品编号","产品名称","退货数量","退货单价","退货金额","客户备注","中转仓","配送人员","配送人电话",
		"退货地址"};
		cellDates(cellValues, style, row);

		List<TdReturnReport> returnReportList = tdReturnReportService.searchReturnReport(begin, end, cityName, diyCode, username);

		if (returnReportList != null && returnReportList.size()>0)
		{
			Integer i = 1;
			for (TdReturnReport returnReport : returnReportList) {
				row = sheet.createRow(i);
				if (returnReport.getDiySiteName() != null)
				{//退货门店
					row.createCell(0).setCellValue(returnReport.getDiySiteName());
				}
				if (returnReport.getOrderNumber() != null)
				{//原订单号
					row.createCell(1).setCellValue(returnReport.getOrderNumber());
					row.createCell(9).setCellValue(returnReport.getRealName());//客户名称 
					row.createCell(10).setCellValue(returnReport.getUsername());// 客户电话
					row.createCell(6).setCellValue(returnReport.getSellerRealName());//导购
					//					        	row.createCell(16).setCellValue(returnReport.getCashCoupon());//退现金卷金额
					//					            row.createCell(17).setCellValue(returnReport.getProductCoupon());//退产品卷金额
					row.createCell(20).setCellValue(returnReport.getShippingAddress());//退货地址
					row.createCell(18).setCellValue(returnReport.getDeliverRealName());//配送人员
					row.createCell(19).setCellValue(returnReport.getDeliverUsername());//配送人员电话

				}
				if (returnReport.getReturnNumber() != null)
				{//退货单号
					row.createCell(2).setCellValue(returnReport.getReturnNumber());
				}
				if (returnReport.getStatusId() != null)
				{//退货单状态
					if(returnReport.getStatusId().equals(1L)){
						row.createCell(3).setCellValue("确认退货单");
					}
					if(returnReport.getStatusId().equals(2L)){
						row.createCell(3).setCellValue("通知物流");
					}
					if(returnReport.getStatusId().equals(3L)){
						row.createCell(3).setCellValue("验货确认");
					}
					if(returnReport.getStatusId().equals(4L)){
						row.createCell(3).setCellValue("确认退款");
					}
					if(returnReport.getStatusId().equals(5L)){
						row.createCell(3).setCellValue("已完成");
					}
				}
				if (returnReport.getBrandTitle() != null)
				{//品牌
					row.createCell(4).setCellValue(returnReport.getBrandTitle());
				}
				if (returnReport.getCategoryTitle() != null)
				{//商品类别
					row.createCell(5).setCellValue(returnReport.getCategoryTitle());
				}
				if (returnReport.getOrderTime() != null)
				{//订单日期
					row.createCell(7).setCellValue(returnReport.getOrderTime().toString());
				}
				if (returnReport.getCancelTime() != null)
				{//退货日期
					row.createCell(8).setCellValue(returnReport.getCancelTime().toString());
				}
				if (returnReport.getSku() != null)
				{//产品编号
					row.createCell(11).setCellValue(returnReport.getSku());
				}
				if (returnReport.getGoodsTitle() != null)
				{//产品名称
					row.createCell(12).setCellValue(returnReport.getGoodsTitle());
				}
				if (returnReport.getQuantity() != null)
				{//退货数量
					row.createCell(13).setCellValue(returnReport.getQuantity());
				}
				if (returnReport.getPrice() != null)
				{//退货单价
					row.createCell(14).setCellValue(returnReport.getPrice());
				}

				if (returnReport.getQuantity() != null && returnReport.getPrice() != null)
				{//退货总价
					row.createCell(15).setCellValue(returnReport.getQuantity()*returnReport.getPrice());
				}

				if(returnReport.getRemarkInfo() != null){//客户备注
					row.createCell(16).setCellValue(returnReport.getRemarkInfo());
				}
				if(returnReport.getWhNo() != null){//中转仓
					row.createCell(17).setCellValue(changeName(returnReport.getWhNo()));
				}

				i++;
			}
		}
		return workbook;
	}
	
	
}