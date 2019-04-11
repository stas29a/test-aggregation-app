package aggregation.app.services;

import org.asynchttpclient.AsyncHttpClient;
import org.springframework.stereotype.Service;
import static org.asynchttpclient.Dsl.asyncHttpClient;

@Service
public class HttpClientFactory {
    public AsyncHttpClient create()
    {
        return asyncHttpClient();
    }
}
