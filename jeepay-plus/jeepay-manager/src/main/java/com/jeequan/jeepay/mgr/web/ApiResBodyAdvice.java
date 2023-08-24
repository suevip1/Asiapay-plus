/*
 * Copyright (c) 2021-2031
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.mgr.web;

import com.jeequan.jeepay.core.utils.ApiResBodyAdviceKit;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/*
* 功能： 自定义springMVC返回数据格式
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:12
*/
@ControllerAdvice
public class ApiResBodyAdvice implements ResponseBodyAdvice {

    /** 判断哪些需要拦截 **/
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    /** 拦截返回数据处理 */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        //处理扩展字段
        return ApiResBodyAdviceKit.beforeBodyWrite(body);
    }

}
