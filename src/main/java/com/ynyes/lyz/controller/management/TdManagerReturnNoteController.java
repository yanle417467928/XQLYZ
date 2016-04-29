package com.ynyes.lyz.controller.management;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ynyes.lyz.entity.TdManager;
import com.ynyes.lyz.entity.TdManagerRole;
import com.ynyes.lyz.entity.TdOrder;
import com.ynyes.lyz.entity.TdReturnNote;
import com.ynyes.lyz.service.TdCityService;
import com.ynyes.lyz.service.TdCommonService;
import com.ynyes.lyz.service.TdDeliveryInfoDetailService;
import com.ynyes.lyz.service.TdDiySiteService;
import com.ynyes.lyz.service.TdGoodsService;
import com.ynyes.lyz.service.TdManagerLogService;
import com.ynyes.lyz.service.TdManagerRoleService;
import com.ynyes.lyz.service.TdManagerService;
import com.ynyes.lyz.service.TdOrderService;
import com.ynyes.lyz.service.TdPayTypeService;
import com.ynyes.lyz.service.TdPriceCountService;
import com.ynyes.lyz.service.TdReturnNoteService;
import com.ynyes.lyz.service.TdReturnReportService;
import com.ynyes.lyz.service.TdUserService;
import com.ynyes.lyz.service.TdUserTurnRecordService;
import com.ynyes.lyz.util.SiteMagConstant;

@Controller
@RequestMapping(value = "/Verwalter/returnNote")
public class TdManagerReturnNoteController extends TdManagerBaseController{

	@Autowired
	TdReturnNoteService tdReturnNoteService;

	@Autowired
	TdManagerLogService tdManagerLogService;

	@Autowired
	private TdOrderService tdOrderService;

	@Autowired
	private TdUserService tdUserSerrvice;

	@Autowired
	private TdDiySiteService tdDisSiteService;

	@Autowired
	private TdPayTypeService tdPayTypeService;

	@Autowired
	private TdUserTurnRecordService tdUserTurnRecordService;

	@Autowired
	private TdCommonService tdCommonService;
	
	@Autowired
	private TdManagerService tdManagerService;
	
	@Autowired
	private TdManagerRoleService tdManagerRoleService;
	
	@Autowired
	private TdGoodsService tdGoodsService;
	
	@Autowired
	private TdDeliveryInfoDetailService tdDeliveryInfoDetailService;
	
	@Autowired
	private TdCityService tdCityService;
	
	@Autowired
	private TdDiySiteService tdDiySiteService;
	
	@Autowired
	private TdReturnReportService tdReturnReportService;
	
	@Autowired
	private TdPriceCountService tdPriceCountService;
	
	// 列表
	@RequestMapping(value = "/{type}/list")
	public String list(@PathVariable String type, Integer page, Integer size, String keywords, String __EVENTTARGET,
			String __EVENTARGUMENT, String __VIEWSTATE, Long[] listId, Integer[] listChkId, Double[] listSortId,
			ModelMap map, HttpServletRequest req) {
		String username = (String) req.getSession().getAttribute("manager");
		if (null == username) {
			return "redirect:/Verwalter/login";
		}
		
		TdManager tdManager = tdManagerService.findByUsernameAndIsEnableTrue(username);
		TdManagerRole tdManagerRole = null;
		if (tdManager != null && tdManager.getRoleId() != null)
		{
			tdManagerRole = tdManagerRoleService.findOne(tdManager.getRoleId());
		}
		if (tdManagerRole == null)
		{
			return "redirect:/Verwalter/login";
		}

		if (null != __EVENTTARGET) {
			if (__EVENTTARGET.equalsIgnoreCase("btnDelete"))
			{
				btnDelete(type, listId, listChkId);

				if (type.equalsIgnoreCase("returnNote")) 
				{
					tdManagerLogService.addLog("delete", "删除退货单", req);
				}

			}
			if (__EVENTTARGET.equalsIgnoreCase("btnPage"))
			{
				if (__EVENTARGUMENT != null)
				{
					try
					{
						page = Integer.parseInt(__EVENTARGUMENT);
					}
					catch (Exception e) 
					{
						// TODO: handle exception
						page = 0;
					}
				}
			}

		}

		if (null == page || page < 0) {
			page = 0;
		}

		if (null == size || size <= 0) {
			size = SiteMagConstant.pageSize;
			;
		}

		map.addAttribute("page", page);
		map.addAttribute("size", size);
		map.addAttribute("keywords", keywords);
		map.addAttribute("__EVENTTARGET", __EVENTTARGET);
		map.addAttribute("__EVENTARGUMENT", __EVENTARGUMENT);
		map.addAttribute("__VIEWSTATE", __VIEWSTATE);

		if (null != type) {
			if (type.equalsIgnoreCase("returnNote")) //
			{
//				if (tdManagerRole.getTitle().equalsIgnoreCase("门店")) 
//				{
//					String diyCode = tdManager.getDiyCode();
//					TdDiySite tdDiySite = tdDisSiteService.findByStoreCode(diyCode);
//					map.addAttribute("returnNote_page",tdReturnNoteService.findBySiteIdAndKeywords(tdDiySite.getId(), keywords, page, size));
//				}
//				else
//				{
//					if (StringUtils.isNotBlank(keywords))
//					{
//						map.addAttribute("returnNote_page", tdReturnNoteService.searchAll(keywords, page, size));
//					}
//					else
//					{
//						map.addAttribute("returnNote_page", tdReturnNoteService.findAll(page, size));
//					}
//				}
				String siteName = tdReturnNoteService.findSiteTitleByUserName(username);
				siteName = StringUtils.isNotBlank(siteName) ? siteName : keywords;
				String keyword = StringUtils.isNotBlank(keywords) ? keywords : "";
				if (StringUtils.isNotBlank(siteName))
				{
					map.addAttribute("returnNote_page", tdReturnNoteService
							.findByDiySiteTitleAndOrderNumberOrReturnNumberOrUsername(siteName, keyword, page, size));
				}
				else if (StringUtils.isNotBlank(keyword))
				{
					map.addAttribute("returnNote_page", tdReturnNoteService.searchAll(keyword, page, size));
				}
				else
				{
					map.addAttribute("returnNote_page", tdReturnNoteService.findAll(page, size));
				}
				//城市和门店信息
				if (tdManagerRole.getIsSys()){
					map.addAttribute("diySiteList",tdDiySiteService.findAll());
					map.addAttribute("cityList", tdCityService.findAll());
				}
				return "/site_mag/returnNote_list";
			}

		}
		return "/site_mag/returnNote_list";
	}
	
	@RequestMapping(value = "/{type}/edit")
	public String edit(@PathVariable String type, Long id, String __VIEWSTATE, ModelMap map, HttpServletRequest req) {
		String username = (String) req.getSession().getAttribute("manager");
		if (null == username) {
			return "redirect:/Verwalter/login";
		}

		map.addAttribute("__VIEWSTATE", __VIEWSTATE);

		if (null != type) {
			if (type.equalsIgnoreCase("returnNote")) // 支付方式
			{
				if (null != id) {
					map.addAttribute("returnNote", tdReturnNoteService.findOne(id));
				}

				return "/site_mag/returnNote_edit";
			}
		}

		return "/site_mag/returnNote_edit";
	}

	/**
	 * 修改退货单状态
	 * 
	 * @author Max
	 * 
	 */
	@RequestMapping(value = "/param/edit", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> returnNoteParam(String returnNumber, String type, String data, HttpServletRequest req) {
		Map<String, Object> res = new HashMap<>();

		res.put("code", 1);

		String username = (String) req.getSession().getAttribute("manager");

		if (null == username) {
			res.put("message", "请重新登录");
			return res;
		}

		if (null != returnNumber && !returnNumber.isEmpty() && null != type && !type.isEmpty()) {
			TdReturnNote returnNote = tdReturnNoteService.findByReturnNumber(returnNumber);

			if (returnNote == null)
			{
				res.put("message", "参数错误");
				return res;
			}
			// 通知物流
			if ("informDiy".equalsIgnoreCase(type)) 
			{
				// 配送单——到店退
				if (returnNote.getTurnType() == 2 && returnNote.getStatusId() != null && returnNote.getStatusId() == 1) 
				{
					// 生成收货通知
					tdCommonService.sendBackToWMS(returnNote);
					if (returnNote.getStatusId() == 1) 
					{
						returnNote.setStatusId(2L);
					}
				}
			}
			//确认收货
			else if("btnConfirm".equalsIgnoreCase(type))
			{
				 returnNote.setManagerRemarkInfo(returnNote.getManagerRemarkInfo() + "后台确认收货("+username+"");
				 if (returnNote.getStatusId() != null && returnNote.getStatusId() == 2L) 
				 {
					 returnNote.setStatusId(3L);
				 }
			}
			// 确认验货
			else if ("examineReturn".equalsIgnoreCase(type)) {
				if (returnNote.getStatusId().equals(3L)) {
					returnNote.setStatusId(4L);
					returnNote.setCheckTime(new Date());
				}
			}
			// 确认退款
			else if ("refund".equals(type)) 
			{
				if (returnNote.getStatusId().equals(3L)) 
				{
					// 查找订单
					TdOrder order = tdOrderService.findByOrderNumber(returnNote.getOrderNumber());
					if (order != null && order.getStatusId() != null && order.getStatusId() == 9L)
					{
						tdPriceCountService.actAccordingWMS(returnNote, order.getId());
						order.setStatusId(12L);
						tdOrderService.save(order);
						
					}
					returnNote.setReturnTime(new Date());
					returnNote.setStatusId(5L);// 退货单设置已完成
				}
			}

			tdReturnNoteService.save(returnNote);
			res.put("code", 0);
			res.put("message", "修改成功");
			return res;

		}

		res.put("message", "参数错误");
		return res;
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String save(TdReturnNote tdReturnNote, ModelMap map, HttpServletRequest req) {
		String username = (String) req.getSession().getAttribute("manager");
		if (null == username) {
			return "redirect:/Verwalter/login";
		}

		if (null == tdReturnNote.getId()) {
			tdManagerLogService.addLog("add", "新增退货单", req);
		} else {
			tdManagerLogService.addLog("edit", "修改退货单", req);
		}
		tdReturnNoteService.save(tdReturnNote);

		return "redirect:/Verwalter/returnNote/returnNote/list";
	}
	

	@ModelAttribute
	public void getModel(@RequestParam(value = "returnNoteId", required = false) Long returnNoteId, Model model) {
		if (null != returnNoteId) {
			model.addAttribute("tdReturnNote", tdReturnNoteService.findOne(returnNoteId));
		}

	}

	private void btnDelete(String type, Long[] ids, Integer[] chkIds) {
		if (null == type || type.isEmpty()) {
			return;
		}

		if (null == ids || null == chkIds || ids.length < 1 || chkIds.length < 1) {
			return;
		}

		for (int chkId : chkIds) {
			if (chkId >= 0 && ids.length > chkId) {
				Long id = ids[chkId];

				if (type.equalsIgnoreCase("returnNote")) {
					tdReturnNoteService.delete(id);
				}

			}
		}
	}

}
