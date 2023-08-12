package pg.paymentgateway.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import pg.paymentgateway.entity.Merchant;
import pg.paymentgateway.repository.MerchantRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Slf4j
public class Interceptor implements HandlerInterceptor {

    @Autowired
    private MerchantRepository merchantRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("-----------------------interceptor start-----------------------");
        log.info("Request URI ==> {}", request.getRequestURI());

        // application/json 방식만 허용
        if(!request.getContentType().equals("application/json")){
            response.setStatus(400);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"resultCode\":\"False\",\"resultMessage\":\"허용되지않는 Content-Type\"}");

            return false;
        }

        // Authorization 검증
        String authorization = request.getHeader("Authorization");
        if(StringUtils.isEmpty(authorization)){
            response.setStatus(400);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"resultCode\":\"False\",\"resultMessage\":\"Authorization 값이 없습니다.\"}");

            return false;
        }else{
            Optional<Merchant> merchant = Optional.ofNullable(merchantRepository.findMerchantByPaymentKey(authorization));

            if(!merchant.isPresent()){
                response.setStatus(401);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"resultCode\":\"False\",\"resultMessage\":\"Authorization 인증 실패.\"}");
            }
        }

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        log.info("-----------------------interceptor end-----------------------");
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
