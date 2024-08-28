package pl.myapp.webcrawler;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpResponse;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.myapp.webcrawler.domain.CrawledUrl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class WebCrawlerService {

    private final CrawledUrlRepository repository;
    private final String accessToken;

    String basicAuth = "cGxhdGZvcm1hOnBsYXRmb3JtYTk5";
    String cookie = "pu-cookie--law=true; pu-alpha=false; __Host-next-auth.csrf-token=948376077c2a4abd460df8ddc98ad65f563e1fa79cd6ed1d43ae7bf5e9f0f622%7C1b54a9535112a74215c2ddb22f74b255b590751f2bf017e516b37ff93b698dcd; __Secure-next-auth.callback-url=https%3A%2F%2Fportal.pm-torun.pl; __Secure-next-auth.session-token.0=eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2R0NNIn0..3qRPkOdpDEzBvI0J.QD0IksuVNBTHwWyNPzgA2dw674VkVhEzRKNFYMrCheOynPEZhh2Ox9R0u2I4Bzn6oSHAUvM--l99HNkCRWJG3-hKpR5_EKW3612AIUpIl4EyM_ShqRKThHk_mg7PqbuY2oR9jvE1_4fxYytxouuF5iuBoGTadTv9Z7T3L_0wyGRUpmwKw_WYjQKru4Ty_WxCDkgDbbVRthi5TOA4FZfr8xCgIwvXPNcNWCz6AHFUI1TnAukKJkfYAcdSyqDXNQpzvCjA6vNDZNefLPB4b8tFOEOWD5i8CC7onVZAvHLw5ZyOSpzs_7LwzZyhLSxcrnhowFkszTC3O_VktJglfwm5HWJunPkDaHQb2M13f2BmZVmGV2FQRP9nwc5KFyvUb2byoQ0x7IU2hr4V1BE0fqh-cpkvB54h4cQ36fCzMj27vZRXqOSrsfGx0zcv2V8cHXvrsPtNYG0QlgyU4iY4GYGSyGKlwgAoqGlw1SaLNIbRlbhrWORV2Xv5Pk0_ZRCNQQdzetedwF_AwmIYpQOR8xBbn_dUnL4RznPMINEelXWfhva80zFhqKwvmlc2oC244fyqQ9HCGeFjp9cPa2p6NvO8_vjYM-LVBmn77bDyuOw_201AMu5m60xfERcfeCYUhCW2PST2PDCn4UQAzzjb9uQrM1DQIMWLfI5Fdy9VCQPKzqBhOknuPnpD4bsQhgNfeSkhoVuZtrJzCrv1wN1qAQpzmf7SCUgnS5bR_Cx3uNsVAbsURbxaGGoUgbljI-3fxlqhjnFHHjmDrQOOJiLR9x22GD6G5ZMnbSgV4kQl-uAOcwbI4Pmb8OzrUZ_J5ADY7646-IfwkuC-TFPxvroOD9vUO-CN54M4jSGgpYC6SZoouF5-z-slBuaM8wkEiBPEk9IzWuxeZ-_hRJY5YYtWuke3Pw-wvKFMex_3z1dOHIsSrPQrjuCFM_llCPnL3EdYb-2XxeMuIhoRv5HNstPyn2O8emmovy-cSVaAlgP0iCgfYWkxccrV1tn0BoBtLgAKI4Bir0MBrQWVvEohWG3guounwzbahJ2DSBUP4C3ILQns0Bwuvsp8yWYY71VbCoUnIY-xTgFVhZwcgxXdQiAhB-cSiDtX6Sku5e_s3X4wIRRtKZUfts6Rq4aCAYLYAr2MccweS8KQvW3aHy0-w5MZjVZephMUWi3nSYd4JE7s8mC913q9mC5LMnckXzKMp1W0AWSL4yqFfr3FUNP1evD3fGmxP8g_HAAJDY20JPsTVeX8kvjAAeAV2FLZj-dsY_kiSo4-WvfGCjTXMc2EOXuRXROlLO35EMahNpaCJRmr673-hUEjUzQ7mCMbdf9CvnRQDKMjPzAT70T29b2nB2q0uZ7FOi7E5_n7inc-BP1a1KrV1uk8EDu5Vg39b3VVoNT4uSdLfLWLzC5TkQiYlhru_Cdu3Gb4RZiDv5VywD9Z5Fihw8xi0MfiGElemCJHeNIS78ShzUr3K50dpUjXy0v8xAK1LCqusBj3FoBUO3B_74Xhx5Fxhvl4Wekpp1oVQ4az9bTqwm7mJborAZxiwOo_JvwFefXMiYP9xbzUzB9F7pWqRNQPNsk_DtgblpAxqqDZ7trzxLRqCj_LLvMm3TKeUv_qTGStbE6IEYWfNA-MvpW3Ez7y2whs-L9F8wQ7s7W6DJ6A2QJCkRwyZjmQVOgdOjVCFZtdDS28JMioB5v2DfCLB7_mcFVmktDkDBzCFo6lU5Vz2dwP-i4AXrB4rPGdVbKBWC_905oowrjgLBs65AD8QdFB0v-o4aEe4OHJjKt6BhhM6D0OwSSLLk0sgx6Rdp3NoOPTX0s4Oyz83S9SXmYHaDWDnolAGS8D7Ye51XiYEDF6X_qXw92nV1nQDqK2-pCchQXVV-ZVVBfVXisrGAeyrIwn6HBMXryICUSUilsk_EX4fxVHlcCkwa2H-xKnz1OI7VgUd8ypY5hSsDu7TBEZUEq-o_N2luWr60bhY4VpWr_6dt4Wt1GhU-XcqzF-ilKiGghzuML-IEs8Raym2c6hYYXOFd-ufjauYr7pCm-OV5TlhVQQ7QM3gfXvyASzzUkOLVRTjOQYuHO3L4FP6rZmfZ5WfXYDuTJItRBMQnF8_zNJJFrHPXsorRIfNkyhf8Q-whJhUV1QDSJwx0HeulrxlxM4z5yQVbjwWf_FLutg9m4TawU8Yoz3vxOuZIVk4Ka9BT7yHjxmO_gctX2dMM4McgVA8GLO1Qd_DeUDWHsEJsptsqEsdvJFykiAnQW5R4wq8uWg7ht0q6FTQLSzpDYXTnJIokD_QzRSHlxX9L2VGcnWRyKiCX-j4E83sLSnbVYfn6E20bYsDR9enQ9d3A0zl7HNVzYKplNbbIUQS5b0M0oktkF44KYjxcobebptwsqry59aiJs_yB8esIwXZr7NdIIUZJapufqKjda7pea_UYHbBH1ZfcC4nqMD3hA_xGKa4ymEmcSFlyEzhk6E4OW9xY4ynhxeU8AAfsNm9fyBxL33gJ-v0qD0PQDjZb5zu8MnKFp6h0gedtYn-UsW3InjDilm9XwocG5McTiUNUgicrJ_LNrFPJqNtZECJq4t-k68FXRBUJrnvDhgU0iVSVgzVEtmTv63a_S55wUL9MkCA8VWN-VlamI7qBh-oZ6axJDiCzpCcO1B27F62oELYHSZHCPFRBCAo-k0kmUQmMS2ipMzJCKqaxZ_Nsq3Sc2GWVv2t6VZGBnTFciz0mgWrS0EZuiDvNSqDXkcCZXDLlBXLyFyUzQj5fR50ik3UeThZCehp_iNoXKr4rAIa3V5tQjpCx5hkHRwNjeTWRlRz1iR26q1U2_2qGQINp5ZnF00Cmil69hNWW3DwCifZ3VE2a86C77q-EjZRikAlShHcPz-mh0-CbkrkZZDUj7qdfksa52b3q2WoEywv74DVy_vT9HAYRY4euWK_u_vHtLXJh6DCghZXvcj794M1ctf_YMRQwJSCViThL90G9OO-yV603enUM2YE7jMkiNHoJKiVS_8EumzWoFeS6cq6zD54JYRthzLj-BXMThI7p022GGILK0ZGr6R3QrbjLYCLPiwteUMaqBVm4d-6pcywyzAQjPlYQl0nxfH0gnRCA46R0KPpQq8D7EaP1W7ZEV7Z4Vvlq9xDTwOKIoF8J71mRYiB5vaQZmwHpz96VqS3Ewjc4bhzFzowvaACkIkWcHv3khYCTAv7FuRRThjhaD5p1tHiYrsy56OC4dEjMDRRfcqsmbfmGuDDZaGtgcPqq3XAbtdmIvEyEpdAr3tvkHySNkH9GeS_s4OBUP92LMSE7iMrADi0S8ae_ZiZgX06Iw9L9JSTbQ6Mc9ZWLtAG2Vfa4A9qPo8Oxyc4FfzDmFPHZ7Afm1d-VLJVJzaeui2ucK04FOTwXkrxD0lh-Y0Jc3zfANo40M83rteu70K6thUTKkrf-7x3DzsnRfLXK5oWJvfka2QevdbjwUYH5BV9WDduyQonBCJmTAigMEYgwCWYxfaBaoo2ckzgc9NbaDWrCd8lETkEXss-I2ry30PcYoiLyhVi_Dk_pMOrzHYYJXGMiozHptNzQ1swu3zb-lCAUDoxprEmd49Q1TBe_ZJtVEMdEIYkLehwxFONcZyypqHHZrl6O9RVz_NwX-N9Emo_f1akM8v4kHoYGFeeHyMOPkMedFW-vBxoCAw_0tCcT05QpPTrh941ZdPJ9qJuMl2yQVIHJLp8olUquysvEFzqXaUQfTdM9HDH-kyp_hQCm68YR2lVbobhwOXhDI93o09y_y_Yi3bf0b7IQiYcHD-jtPaWqwR97RibXl7d0B4lm8PgOj6rSzW2EsOz5k8Ux9PL-jgfVsNer7rtmQGLyxUtkF6UIWxYtjocpKV0xqOrsUvTcLdrLK; __Secure-next-auth.session-token.1=YM9IQs5TMZ6cwByjtxH_NHFLJfvVLBgAm-qw1_6ujA4zOhi4t4UTbZcLR_Al_efSgfq-AKeR7e1y-HNcp34ZLUdmKb7ENosBDSZnUETxIIDZf4OVgaxF5s42Evx71y-e6BtQFcY8BTW4F797DNIRvezLPCgl1ASKuTS8qlGLLkZFdwL5prPYgpjVF0GYZ14ixKmFcwPVZbNLne0H0HdK80TO0HRBTsA0UaULEOpplEVOA0v4gBJTYd2EfC2cOMFTLtJD4owI4INzoJJVSk0On3wx_mpG9-nn6wYL_90tQ_Xqhyoaf-a4ig5MvhpbFsbavEsv7MHFCWRpYUqTQ4bV6sb4TNhPPz_UkeS27wXLrkG-wmSb5N5R-x55edklam0pMz5tzYReCNIk6uTGvRScZkoY8tzDptRhKMaPHaxpjnu-eKadBl4HEpJMkB7uKT3k9b9joaPtUdOm57tI3AtGcG7Dk4BvihzbbM6pfGfp6Xe-7OazEMuaqM2ghqZAJnveh0kN-Z34JOQqD1IN9LBDikwU2zuwnbNy72IN3UZd-sEM1qSDqRWy9a6w4Q_QF1PhNaiKRJ9nckqMG64HY3Zv-ZqU.MaL_TdvtcbF-wMiBl0v3ww; applicationContext={\"fontSize\":\"normal\",\"highContrast\":false,\"token\":\"eyJ4NXQiOiJNMlppTWpkbE0yWXhOV1l6WkdZNVl6UXlPRE00TmpNMk0yWXlZMlV3WWprME1HSm1PR1UwWW1GalpHVXpOR1U0Wmpaak16SXlOakk0WlRsbFltWTRZZyIsImtpZCI6Ik0yWmlNamRsTTJZeE5XWXpaR1k1WXpReU9ETTROak0yTTJZeVkyVXdZamswTUdKbU9HVTBZbUZqWkdVek5HVTRaalpqTXpJeU5qSTRaVGxsWW1ZNFlnX1JTMjU2IiwidHlwIjoiYXQrand0IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJDSVRJWkVOU1wvNmJhZjRiY2MtMTA0NC00MzFlLTg4NjAtYzU2Y2ZjMzczYjFlIiwiYXV0IjoiQVBQTElDQVRJT05fVVNFUiIsInBjcHIiOiJQTV9QUk9GSUxfSU5EWVdJRFVBTE5ZX0FETUlOSVNUUkFUT1IiLCJiaW5kaW5nX3R5cGUiOiJzc28tc2Vzc2lvbiIsInBja2lwIjoiWFhYLkxPR0lOR09WIiwiaXNzIjoiaHR0cHM6XC9cL2xvZ2luLnBtLXRvcnVuLnBsXC9vYXV0aDJcL3Rva2VuIiwiZ2l2ZW5fbmFtZSI6Im8wMSIsImNsaWVudF9pZCI6Ik5ud09ZTU1IdXBicnVrOV9VM3ROc2cyUkN0MGEiLCJwY3N0ayI6IlBMQVRGT1JNQV9NSUVKU0tBIiwiYXVkIjoiTm53T1lNTUh1cGJydWs5X1UzdE5zZzJSQ3QwYSIsInBjYXBrIjoiT1hfVEVfMTk1OTMiLCJuYmYiOjE3MjM0NTYxMDcsInBjaXZsIjoiMSIsImF6cCI6Ik5ud09ZTU1IdXBicnVrOV9VM3ROc2cyUkN0MGEiLCJzY29wZSI6ImVtYWlsIGludGVybmFsX2xvZ2luIG9wZW5pZCBwYyBwcm9maWxlIiwicGNpZCI6IjIyZDQyZjJhLWFkYjgtNGUxMS04ZmE3LWEwZjdhNjhjZTMzOSIsImV4cCI6MTcyMzQ1OTcwNywiaWF0IjoxNzIzNDU2MTA3LCJwY2F0ayI6IlBMQVRGT1JNQV9NSUVKU0tBLk1OUF9UT1JVTiIsImZhbWlseV9uYW1lIjoiVGVzdCBvMDEiLCJiaW5kaW5nX3JlZiI6ImQ2ZGMxNTA5N2ExMDRhNDkyODNiNWFmN2E2MjBjZWI5IiwianRpIjoiMjMxNjNhZWEtM2I0ZS00NGNmLWFjMjctNzVkYzc2NjIzOGY4IiwiZW1haWwiOiJvMDFAZ21pYWwuY29tIiwicGN0bCI6Inp3cCJ9.h-m4IbJYXl41EzI56p-nuwsDsr8BkoJ-QK4m_PQb9DDK7QOQncu72MDWqwNn3os-R7cb3zdMyG-ku9iXNGZjxtzRey0ZDEp6uNEOmqVLZ1FHbOc2Kzv-c-jnRddsNucZqkGBvnCA1qR_UL9s1H35Z5SUIPvOi_lrH8UQzgr-A-Jvr-QYnRaF6h9AQjiN24c1q_EpvfY7aPlBPTC6lPO1m2ucyLIR07iy-3p5ivlpCDxrebCSvOwuYDl3HpEwxMT-sk9wJR1SEjO_nOuLZ-KYXVCahky5msPg_9qoUjjv8b41iFE7EA3k29PsAKYbvpkmT_pbhv8k7pDIZaPk_-QX4Q\"}";

    public WebCrawlerService(final CrawledUrlRepository repository,
                             @Value("${params.accessToken:}") final String accessToken) {
        this.repository = repository;
        this.accessToken = accessToken;
    }

    public void crawlWebpage(final String baseUrl) throws IOException, URISyntaxException {
        crawlUrl(baseUrl, new HashSet<>());
    }

    private void crawlUrl(final String stringUrl, Set<String> visitedUrls) throws IOException, URISyntaxException {
        System.out.println("Crawling url: " + stringUrl);

        // Skip if the URL has already been visited
        if (visitedUrls.contains(stringUrl) || repository.existsByUrl(stringUrl)) {
            return;
        }

        // Mark this URL as visited
        visitedUrls.add(stringUrl);

        // Fetch the HTTP status code
        int statusCode = fetchUrlStatus(stringUrl);
        saveUrl(stringUrl, statusCode);

        // Fetch and parse the document

        String profileToken = "eyJ4NXQiOiJNMlppTWpkbE0yWXhOV1l6WkdZNVl6UXlPRE00TmpNMk0yWXlZMlV3WWprME1HSm1PR1UwWW1GalpHVXpOR1U0Wmpaak16SXlOakk0WlRsbFltWTRZZyIsImtpZCI6Ik0yWmlNamRsTTJZeE5XWXpaR1k1WXpReU9ETTROak0yTTJZeVkyVXdZamswTUdKbU9HVTBZbUZqWkdVek5HVTRaalpqTXpJeU5qSTRaVGxsWW1ZNFlnX1JTMjU2IiwidHlwIjoiYXQrand0IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJDSVRJWkVOU1wvNmJhZjRiY2MtMTA0NC00MzFlLTg4NjAtYzU2Y2ZjMzczYjFlIiwiYXV0IjoiQVBQTElDQVRJT05fVVNFUiIsInBjcHIiOiJQTV9QUk9GSUxfSU5EWVdJRFVBTE5ZX0FETUlOSVNUUkFUT1IiLCJiaW5kaW5nX3R5cGUiOiJzc28tc2Vzc2lvbiIsInBja2lwIjoiWFhYLkxPR0lOR09WIiwiaXNzIjoiaHR0cHM6XC9cL2xvZ2luLnBtLXRvcnVuLnBsXC9vYXV0aDJcL3Rva2VuIiwiZ2l2ZW5fbmFtZSI6Im8wMSIsImNsaWVudF9pZCI6IlhWYWdlTGxzQlBBN3dwV19JU0RfY3ZZNzBfa2EiLCJwY3N0ayI6IlBMQVRGT1JNQV9NSUVKU0tBIiwiYXVkIjoiWFZhZ2VMbHNCUEE3d3BXX0lTRF9jdlk3MF9rYSIsInBjYXBrIjoiT1hfVEVfMTk1OTMiLCJuYmYiOjE3MjM0NTYxMjIsInBjaXZsIjoiMSIsImF6cCI6IlhWYWdlTGxzQlBBN3dwV19JU0RfY3ZZNzBfa2EiLCJzY29wZSI6ImVtYWlsIGludGVybmFsX2xvZ2luIG9wZW5pZCBwYyBwcm9maWxlIiwicGNpZCI6IjIyZDQyZjJhLWFkYjgtNGUxMS04ZmE3LWEwZjdhNjhjZTMzOSIsImV4cCI6MTcyMzQ1OTcyMiwiaWF0IjoxNzIzNDU2MTIyLCJwY2F0ayI6IlBMQVRGT1JNQV9NSUVKU0tBLk1OUF9UT1JVTiIsImZhbWlseV9uYW1lIjoiVGVzdCBvMDEiLCJiaW5kaW5nX3JlZiI6ImQ2ZGMxNTA5N2ExMDRhNDkyODNiNWFmN2E2MjBjZWI5IiwianRpIjoiYzM0YmZiOTgtZTc0OC00MWU1LThjMGItNjFmMjBlZjA4NTRjIiwiZW1haWwiOiJvMDFAZ21pYWwuY29tIiwicGN0bCI6Inp3cCJ9.GNfWiF-viIyaSWUHhz75Ci14TfA4G-WmLR9V2rgsfLYnBEzmz4FM1N71doMM56mF_LYPHeWmz8vvG-_l9n9yPR4ByplrL4xORnTtT3ZtfPJNYt_QGVbASRoK9vX3mFwxwymfeexGg7EPk1QugnU9v4O5z2l5KcOKdwm50qUrdLjzK8SuAzpHBKUarF_RFDQFiU-3PH0_hsn40I3JZZ54z4p5c2vU9DxBOnLH2gSl_w6oXqd0WwQ7tFY7Hmc1ucaHcdAql6cesNq5tiu3lpstv8BferbwfB4rw2yzc3adILLlaIsaIEptOt9r5kufC-Ik5kxUjFtzgk1goGHlpZCkjg";
        String callbackUrl = "https%3A%2F%2Fportal.pm-torun.pl";
        String sessionToken0 = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2R0NNIn0..3qRPkOdpDEzBvI0J.QD0IksuVNBTHwWyNPzgA2dw674VkVhEzRKNFYMrCheOynPEZhh2Ox9R0u2I4Bzn6oSHAUvM--l99HNkCRWJG3-hKpR5_EKW3612AIUpIl4EyM_ShqRKThHk_mg7PqbuY2oR9jvE1_4fxYytxouuF5iuBoGTadTv9Z7T3L_0wyGRUpmwKw_WYjQKru4Ty_WxCDkgDbbVRthi5TOA4FZfr8xCgIwvXPNcNWCz6AHFUI1TnAukKJkfYAcdSyqDXNQpzvCjA6vNDZNefLPB4b8tFOEOWD5i8CC7onVZAvHLw5ZyOSpzs_7LwzZyhLSxcrnhowFkszTC3O_VktJglfwm5HWJunPkDaHQb2M13f2BmZVmGV2FQRP9nwc5KFyvUb2byoQ0x7IU2hr4V1BE0fqh-cpkvB54h4cQ36fCzMj27vZRXqOSrsfGx0zcv2V8cHXvrsPtNYG0QlgyU4iY4GYGSyGKlwgAoqGlw1SaLNIbRlbhrWORV2Xv5Pk0_ZRCNQQdzetedwF_AwmIYpQOR8xBbn_dUnL4RznPMINEelXWfhva80zFhqKwvmlc2oC244fyqQ9HCGeFjp9cPa2p6NvO8_vjYM-LVBmn77bDyuOw_201AMu5m60xfERcfeCYUhCW2PST2PDCn4UQAzzjb9uQrM1DQIMWLfI5Fdy9VCQPKzqBhOknuPnpD4bsQhgNfeSkhoVuZtrJzCrv1wN1qAQpzmf7SCUgnS5bR_Cx3uNsVAbsURbxaGGoUgbljI-3fxlqhjnFHHjmDrQOOJiLR9x22GD6G5ZMnbSgV4kQl-uAOcwbI4Pmb8OzrUZ_J5ADY7646-IfwkuC-TFPxvroOD9vUO-CN54M4jSGgpYC6SZoouF5-z-slBuaM8wkEiBPEk9IzWuxeZ-_hRJY5YYtWuke3Pw-wvKFMex_3z1dOHIsSrPQrjuCFM_llCPnL3EdYb-2XxeMuIhoRv5HNstPyn2O8emmovy-cSVaAlgP0iCgfYWkxccrV1tn0BoBtLgAKI4Bir0MBrQWVvEohWG3guounwzbahJ2DSBUP4C3ILQns0Bwuvsp8yWYY71VbCoUnIY-xTgFVhZwcgxXdQiAhB-cSiDtX6Sku5e_s3X4wIRRtKZUfts6Rq4aCAYLYAr2MccweS8KQvW3aHy0-w5MZjVZephMUWi3nSYd4JE7s8mC913q9mC5LMnckXzKMp1W0AWSL4yqFfr3FUNP1evD3fGmxP8g_HAAJDY20JPsTVeX8kvjAAeAV2FLZj-dsY_kiSo4-WvfGCjTXMc2EOXuRXROlLO35EMahNpaCJRmr673-hUEjUzQ7mCMbdf9CvnRQDKMjPzAT70T29b2nB2q0uZ7FOi7E5_n7inc-BP1a1KrV1uk8EDu5Vg39b3VVoNT4uSdLfLWLzC5TkQiYlhru_Cdu3Gb4RZiDv5VywD9Z5Fihw8xi0MfiGElemCJHeNIS78ShzUr3K50dpUjXy0v8xAK1LCqusBj3FoBUO3B_74Xhx5Fxhvl4Wekpp1oVQ4az9bTqwm7mJborAZxiwOo_JvwFefXMiYP9xbzUzB9F7pWqRNQPNsk_DtgblpAxqqDZ7trzxLRqCj_LLvMm3TKeUv_qTGStbE6IEYWfNA-MvpW3Ez7y2whs-L9F8wQ7s7W6DJ6A2QJCkRwyZjmQVOgdOjVCFZtdDS28JMioB5v2DfCLB7_mcFVmktDkDBzCFo6lU5Vz2dwP-i4AXrB4rPGdVbKBWC_905oowrjgLBs65AD8QdFB0v-o4aEe4OHJjKt6BhhM6D0OwSSLLk0sgx6Rdp3NoOPTX0s4Oyz83S9SXmYHaDWDnolAGS8D7Ye51XiYEDF6X_qXw92nV1nQDqK2-pCchQXVV-ZVVBfVXisrGAeyrIwn6HBMXryICUSUilsk_EX4fxVHlcCkwa2H-xKnz1OI7VgUd8ypY5hSsDu7TBEZUEq-o_N2luWr60bhY4VpWr_6dt4Wt1GhU-XcqzF-ilKiGghzuML-IEs8Raym2c6hYYXOFd-ufjauYr7pCm-OV5TlhVQQ7QM3gfXvyASzzUkOLVRTjOQYuHO3L4FP6rZmfZ5WfXYDuTJItRBMQnF8_zNJJFrHPXsorRIfNkyhf8Q-whJhUV1QDSJwx0HeulrxlxM4z5yQVbjwWf_FLutg9m4TawU8Yoz3vxOuZIVk4Ka9BT7yHjxmO_gctX2dMM4McgVA8GLO1Qd_DeUDWHsEJsptsqEsdvJFykiAnQW5R4wq8uWg7ht0q6FTQLSzpDYXTnJIokD_QzRSHlxX9L2VGcnWRyKiCX-j4E83sLSnbVYfn6E20bYsDR9enQ9d3A0zl7HNVzYKplNbbIUQS5b0M0oktkF44KYjxcobebptwsqry59aiJs_yB8esIwXZr7NdIIUZJapufqKjda7pea_UYHbBH1ZfcC4nqMD3hA_xGKa4ymEmcSFlyEzhk6E4OW9xY4ynhxeU8AAfsNm9fyBxL33gJ-v0qD0PQDjZb5zu8MnKFp6h0gedtYn-UsW3InjDilm9XwocG5McTiUNUgicrJ_LNrFPJqNtZECJq4t-k68FXRBUJrnvDhgU0iVSVgzVEtmTv63a_S55wUL9MkCA8VWN-VlamI7qBh-oZ6axJDiCzpCcO1B27F62oELYHSZHCPFRBCAo-k0kmUQmMS2ipMzJCKqaxZ_Nsq3Sc2GWVv2t6VZGBnTFciz0mgWrS0EZuiDvNSqDXkcCZXDLlBXLyFyUzQj5fR50ik3UeThZCehp_iNoXKr4rAIa3V5tQjpCx5hkHRwNjeTWRlRz1iR26q1U2_2qGQINp5ZnF00Cmil69hNWW3DwCifZ3VE2a86C77q-EjZRikAlShHcPz-mh0-CbkrkZZDUj7qdfksa52b3q2WoEywv74DVy_vT9HAYRY4euWK_u_vHtLXJh6DCghZXvcj794M1ctf_YMRQwJSCViThL90G9OO-yV603enUM2YE7jMkiNHoJKiVS_8EumzWoFeS6cq6zD54JYRthzLj-BXMThI7p022GGILK0ZGr6R3QrbjLYCLPiwteUMaqBVm4d-6pcywyzAQjPlYQl0nxfH0gnRCA46R0KPpQq8D7EaP1W7ZEV7Z4Vvlq9xDTwOKIoF8J71mRYiB5vaQZmwHpz96VqS3Ewjc4bhzFzowvaACkIkWcHv3khYCTAv7FuRRThjhaD5p1tHiYrsy56OC4dEjMDRRfcqsmbfmGuDDZaGtgcPqq3XAbtdmIvEyEpdAr3tvkHySNkH9GeS_s4OBUP92LMSE7iMrADi0S8ae_ZiZgX06Iw9L9JSTbQ6Mc9ZWLtAG2Vfa4A9qPo8Oxyc4FfzDmFPHZ7Afm1d-VLJVJzaeui2ucK04FOTwXkrxD0lh-Y0Jc3zfANo40M83rteu70K6thUTKkrf-7x3DzsnRfLXK5oWJvfka2QevdbjwUYH5BV9WDduyQonBCJmTAigMEYgwCWYxfaBaoo2ckzgc9NbaDWrCd8lETkEXss-I2ry30PcYoiLyhVi_Dk_pMOrzHYYJXGMiozHptNzQ1swu3zb-lCAUDoxprEmd49Q1TBe_ZJtVEMdEIYkLehwxFONcZyypqHHZrl6O9RVz_NwX-N9Emo_f1akM8v4kHoYGFeeHyMOPkMedFW-vBxoCAw_0tCcT05QpPTrh941ZdPJ9qJuMl2yQVIHJLp8olUquysvEFzqXaUQfTdM9HDH-kyp_hQCm68YR2lVbobhwOXhDI93o09y_y_Yi3bf0b7IQiYcHD-jtPaWqwR97RibXl7d0B4lm8PgOj6rSzW2EsOz5k8Ux9PL-jgfVsNer7rtmQGLyxUtkF6UIWxYtjocpKV0xqOrsUvTcLdrLK";
        String sessionToken1 = "YM9IQs5TMZ6cwByjtxH_NHFLJfvVLBgAm-qw1_6ujA4zOhi4t4UTbZcLR_Al_efSgfq-AKeR7e1y-HNcp34ZLUdmKb7ENosBDSZnUETxIIDZf4OVgaxF5s42Evx71y-e6BtQFcY8BTW4F797DNIRvezLPCgl1ASKuTS8qlGLLkZFdwL5prPYgpjVF0GYZ14ixKmFcwPVZbNLne0H0HdK80TO0HRBTsA0UaULEOpplEVOA0v4gBJTYd2EfC2cOMFTLtJD4owI4INzoJJVSk0On3wx_mpG9-nn6wYL_90tQ_Xqhyoaf-a4ig5MvhpbFsbavEsv7MHFCWRpYUqTQ4bV6sb4TNhPPz_UkeS27wXLrkG-wmSb5N5R-x55edklam0pMz5tzYReCNIk6uTGvRScZkoY8tzDptRhKMaPHaxpjnu-eKadBl4HEpJMkB7uKT3k9b9joaPtUdOm57tI3AtGcG7Dk4BvihzbbM6pfGfp6Xe-7OazEMuaqM2ghqZAJnveh0kN-Z34JOQqD1IN9LBDikwU2zuwnbNy72IN3UZd-sEM1qSDqRWy9a6w4Q_QF1PhNaiKRJ9nckqMG64HY3Zv-ZqU.MaL_TdvtcbF-wMiBl0v3ww";

        Document doc = null;
        try {
            doc = Jsoup.connect(stringUrl)
                    .header("Authorization", "Basic " + basicAuth)
                    .header("Cookie", cookie)
                    .header("Cache-Control", "no-cache")
                    .get();
        } catch (HttpStatusException exception) {
            log.error("Receive 404 for: {}", stringUrl, exception);
        }

        if (doc == null) {
            return;
        }

        // Extract and recursively crawl all links
        Set<String> urls = extractUrls(stringUrl, doc);
        for (String nextUrl : urls) {
            crawlUrl(nextUrl, visitedUrls);
        }
    }

    private Set<String> extractUrls(String baseUrl, Document doc) throws URISyntaxException {
        Set<String> urls = new HashSet<>();
        Elements links = doc.select("a[href]");
        URI baseUri = new URI(baseUrl);

        for (Element link : links) {
            try {
                String href = link.attr("href");
                URI uri = baseUri.resolve(href.replaceAll(" ", "%20"));

                if (uri.getHost() != null && uri.getHost().equals(baseUri.getHost())) {
                    urls.add(uri.toString());
                }
            } catch (Exception exception) {
                log.error("Wrong url! I will skip: {}", link.html(), exception);
            }
        }

        return urls;
    }

    private int fetchUrlStatus(String url) throws IOException, URISyntaxException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpGet request = new HttpGet(new URI(url));
            request.addHeader("Cookie", cookie);
            request.addHeader("Authorization", "Basic " + basicAuth);

            HttpResponse response = httpClient.execute(request);
            return response.getCode();
        }
    }

    @Transactional
    public void saveUrl(String url, int statusCode) {
        CrawledUrl crawledUrl = new CrawledUrl(url, statusCode);
        repository.save(crawledUrl);
    }

}

