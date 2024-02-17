package com.jeequan.jeepay.agent.ctrl;


import com.jeequan.jeepay.agent.config.SystemYmlConfig;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.ctrls.AbstractCtrl;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import com.jeequan.jeepay.service.impl.SysConfigService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

/**
 * 通用ctrl类
 *
 * @author terrfly
 * @modify zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
public abstract class CommonCtrl extends AbstractCtrl {

    @Autowired
    protected SystemYmlConfig mainConfig;

    @Autowired
    private SysConfigService sysConfigService;

    /**
     * 获取当前用户ID
     */
    protected JeeUserDetails getCurrentUser() {

        return (JeeUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * 获取当前代理ID
     **/
    protected String getCurrentAgentNo() {
        return getCurrentUser().getSysUser().getBelongInfoId();
    }

    /**
     * 获取当前用户登录IP
     *
     * @return
     */
    protected String getIp() {
        return getClientIp();
    }

    /**
     * 校验当前用户是否为超管
     *
     * @return
     */
    protected ApiRes checkIsAdmin() {
        SysUser sysUser = getCurrentUser().getSysUser();
        if (sysUser.getIsAdmin() != CS.YES) {
            return ApiRes.fail(ApiCodeEnum.SYS_PERMISSION_ERROR);
        } else {
            return null;
        }

    }

    /**
     * 生成excel文件
     *
     * @param data
     * @throws IOException
     */
    protected void writeExcelStream(List<List> data) throws IOException {
        try {

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=excel_file.xlsx");

            XSSFWorkbook xSSFWorkbook = new XSSFWorkbook();
            Sheet sheet = xSSFWorkbook.createSheet();

            for (int i = 0; i < data.size(); ++i) {
                Row row = sheet.createRow(i);

                for (int j = 0; j < ((List) data.get(i)).size(); ++j) {
                    Cell cell = row.createCell(j);
                    if (((List) data.get(i)).get(j) != null) {
                        cell.setCellValue(((List) data.get(i)).get(j).toString());
                    }
                }
            }
            xSSFWorkbook.write(this.response.getOutputStream());
            xSSFWorkbook.close();
        } catch (Exception var9) {
            throw var9;
        }
    }

}