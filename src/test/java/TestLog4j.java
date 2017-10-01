import com.mauersu.controller.RedisController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

/**
 * Created by zhigang.huang on 2017/9/28.
 */
public class TestLog4j {
    public static Log log = LogFactory.getLog(RedisController.class);

    @Test
    public void test1(){
        log.error("1231");
    }
}
