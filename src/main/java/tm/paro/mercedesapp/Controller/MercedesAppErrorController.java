package tm.paro.mercedesapp.Controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
public class MercedesAppErrorController implements ErrorController {
    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) throws IOException {

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == 204) {
                return "error/error-204";
            } else if (statusCode == 400) {
                return "error/error-400";
            } else if (statusCode == 401) {
                //return "error/error-401";
                MercedesAppController controller=new MercedesAppController();
                controller.getRefreshToken();
                return "main";
            } else if (statusCode == 403) {
                return "error/error-403";
            } else if (statusCode == 404) {
                return "error/error-404";
            } else if (statusCode == 408) {
                return "error/error-408";
            }else if (statusCode == 429) {
                return "error/error-429";
            }else if (statusCode == 500) {
                return "error/error-500";
            }
        }
        return "error";
    }
}
