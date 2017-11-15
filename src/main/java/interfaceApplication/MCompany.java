package interfaceApplication;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import JGrapeSystem.rMsg;
import apps.appsProxy;
import authority.plvDef.plvType;
import check.checkHelper;
import interfaceModel.GrapeDBSpecField;
import interfaceModel.GrapeTreeDBModel;
import security.codec;
import session.session;
import string.StringHelper;

/**
 * 维护单位
 * 
 *
 */
public class MCompany {
	private GrapeTreeDBModel MCompany;
	private GrapeDBSpecField gDbSpecField;
	private session se;
	private JSONObject userInfo = null;
	private String currentWeb = null;
	private Integer userType = null;

	public MCompany() {

		MCompany = new GrapeTreeDBModel();
		gDbSpecField = new GrapeDBSpecField();
		gDbSpecField.importDescription(appsProxy.tableConfig("MCompany"));
		MCompany.descriptionModel(gDbSpecField);
		MCompany.bindApp();
		MCompany.enableCheck();//开启权限检查
		
		se = new session();
		userInfo = se.getDatas();
		if (userInfo != null && userInfo.size() != 0) {
			currentWeb = userInfo.getString("currentWeb"); // 当前用户所属网站id
			userType =userInfo.getInt("userType");//当前用户身份
		}
	}

	/**
	 * 新增维护单位信息
	 * 
	 * @param id
	 * @param info
	 * @return
	 */
	public String AddComp(String info) {
		String result = rMsg.netMSG(100, "新增失败");
		info = checkParam(info);
		if (info.contains("errorcode")) {
			return info;
		}
		JSONObject object = JSONObject.toJSON(info);
		JSONObject rMode = new JSONObject(plvType.chkType, plvType.powerVal).puts(plvType.chkVal, 100);//设置默认查询权限
    	JSONObject uMode = new JSONObject(plvType.chkType, plvType.powerVal).puts(plvType.chkVal, 200);
    	JSONObject dMode = new JSONObject(plvType.chkType, plvType.powerVal).puts(plvType.chkVal, 300);
    	object.put("rMode", rMode.toJSONString()); //添加默认查看权限
    	object.put("uMode", uMode.toJSONString()); //添加默认修改权限
    	object.put("dMode", dMode.toJSONString()); //添加默认删除权限
		if (object != null && object.size() > 0) {
			info = (String) MCompany.data(object).insertEx();
			if(info != null){
				result = rMsg.netMSG(0, "新增成功");
			}
		}
		return result;
	}

	/**
	 * 修改维护单位信息
	 * 
	 * @param id
	 * @param info
	 * @return
	 */
	public String updateComp(String cid, String info) {
		boolean code = false;
		String result = rMsg.netMSG(100, "修改失败");
		info = checkParam(info);
		if (info.contains("errorcode")) {
			return info;
		}
		JSONObject object = JSONObject.toJSON(info);
		if (ObjectId.isValid(cid) && object != null && object.size() > 0) {
			code = MCompany.eq("_id", cid).data(object).updateEx();
				result = code ? rMsg.netMSG(0, "修改成功") : result;
		}
		return result;
	}

	/**
	 * 显示当前网站的维护单位信息
	 * @param idx
	 * @param pageSize
	 * @return
	 */
	public String PageMComp(int idx, int pageSize) {
		long total = 0;
		JSONArray array = null;
		if (!StringHelper.InvaildString(currentWeb)) {
			if (idx > 0 && pageSize > 0) {
				MCompany.eq("wbid", currentWeb);
				array = MCompany.dirty().page(idx, pageSize);
				total = MCompany.count();
			}
		}
		return rMsg.netPAGE(idx, pageSize, total, (array != null && array.size() > 0) ? array : new JSONArray());
	}

	/**
	 * 参数验证
	 * 
	 * @param info
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String checkParam(String info) {
		String temp = "";
		if (!StringHelper.InvaildString(info)) {
			return rMsg.netMSG(1, "参数错误");
		}
		JSONObject object = JSONObject.toJSON(info);
		if (object != null && object.size() > 0) {
			if (object.containsKey("companyEmail")) {
				temp = object.getString("companyEmail");
				if (!checkHelper.checkEmail(temp)) {
					return rMsg.netMSG(2, "邮箱格式错误");
				}
			}
			if (object.containsKey("companyMob")) {
				temp = object.getString("companyMob");
				if (!checkHelper.checkMobileNumber(temp)) {
					return rMsg.netMSG(3, "手机号格式错误");
				}
			}
			if (object.containsKey("companyURL")) {
				temp = object.getString("companyURL");
				temp = codec.DecodeHtmlTag(temp);
				temp = codec.decodebase64(temp);
				object.put("companyURL", temp);
			}
			object.put("wbid", currentWeb);
		}
		return (object != null && object.size() > 0) ? object.toString() : null;
	}
}
