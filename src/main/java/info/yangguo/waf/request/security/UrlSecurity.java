package info.yangguo.waf.request.security;

import com.codahale.metrics.Timer;
import info.yangguo.waf.Constant;
import info.yangguo.waf.WafHttpHeaderNames;
import info.yangguo.waf.model.SecurityConfigIterm;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author:杨果
 * @date:2017/5/11 下午2:24
 * <p>
 * Description:
 * <p>
 * URL路径黑名单拦截
 */
public class UrlSecurity extends Security {
    private static final Logger logger = LoggerFactory.getLogger(UrlSecurity.class);

    @Override
    public boolean doFilter(HttpRequest originalRequest, HttpObject httpObject, List<SecurityConfigIterm> iterms) {
        if (httpObject instanceof HttpRequest) {
            logger.debug("filter:{}", this.getClass().getName());
            HttpRequest httpRequest = (HttpRequest) httpObject;
            String url;
            int index = httpRequest.uri().indexOf("?");
            if (index > -1) {
                url = httpRequest.uri().substring(0, index);
            } else {
                url = httpRequest.uri();
            }
            for (SecurityConfigIterm iterm : iterms) {
                if (iterm.getConfig().getIsStart()) {
                    Timer itermTimer = Constant.metrics.timer("UrlSecurity[" + iterm.getName() + "]");
                    Timer.Context itermContext = itermTimer.time();
                    try {
                        Pattern pattern = Pattern.compile(iterm.getName());
                        Matcher matcher = pattern.matcher(url);
                        if (matcher.find()) {
                            hackLog(logger, originalRequest.headers().getAsString(WafHttpHeaderNames.X_REAL_IP), "Url", iterm.getName());
                            return true;
                        }
                    } finally {
                        itermContext.stop();
                    }
                }
            }
        }
        return false;
    }
}