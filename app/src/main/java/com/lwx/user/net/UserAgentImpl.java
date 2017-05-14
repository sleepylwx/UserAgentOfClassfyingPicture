package com.lwx.user.net;

import android.icu.lang.UScript;

import com.elvishew.xlog.XLog;
import com.lwx.user.App;
import com.lwx.user.db.model.User;
import com.lwx.user.net.rx.StringConverterFactory;
import com.lwx.user.net.rx.UserService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by henry on 17-4-6.
 */
public class UserAgentImpl implements UserAgent{

    UserService userService = null;

    private static UserAgentImpl ourInstance = new UserAgentImpl();

    public static UserAgentImpl getInstance() {
        return ourInstance;
    }

    private UserAgentImpl() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(App.BASE_URL)
                .addConverterFactory(StringConverterFactory.create())
                .build();

        userService = retrofit.create(UserService.class);
    }

    public Observable<User> login(String username, String password){
        return Observable.create(new ObservableOnSubscribe<User>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<User> e) throws Exception {
                try {
                    Response<String> response = userService.login(username, password).execute();
                    String content = response.body();
                    JSONObject jsonObject = new JSONObject(content);

                    if(jsonObject.getBoolean("err")) {
                        e.onError(new Exception(jsonObject.getString("msg")));
                        return;
                    }

                    e.onNext(new User(jsonObject.getLong("uid"),
                            jsonObject.getString("token"),
                            jsonObject.getString("username"),
                            jsonObject.getString("nickname")));
                } catch (IOException|JSONException ex) {
                    e.onError(ex);
                }
            }
        });

    }

    public Completable auth(String token){
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                try {
                    Response<String> response = userService.auth(token).execute();
                    String content = response.body();
                    JSONObject jsonObject = new JSONObject(content);

                    if(jsonObject.getBoolean("err")) {
                        e.onError(new Exception(jsonObject.getString("msg")));
                    }
                    e.onComplete();
                } catch (IOException|JSONException ex) {
                    e.onError(ex);
                }
            }
        });
    }

    public Completable logout(String token){
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                try {
                    Response<String> response = userService.logout(token).execute();
                    String content = response.body();
                    JSONObject jsonObject = new JSONObject(content);

                    if(jsonObject.getBoolean("err")) {
                        e.onError(new Exception(jsonObject.getString("msg")));
                        return;
                    }

                    e.onComplete();
                } catch (IOException|JSONException ex) {
                    e.onError(ex);
                }
            }
        });
    }

    @Override
    public Observable<String> getNickName(Long uid){
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                try {
                    Response<String> response = userService.getNickName(uid.toString()).execute();
                    String content = response.body();
                    JSONObject jsonObject = new JSONObject(content);

                    if(jsonObject.getBoolean("err")) {
                        e.onError(new Exception(jsonObject.getString("msg")));
                        return;
                    }

                    e.onNext(jsonObject.getString("nickname"));
                } catch (IOException|JSONException ex) {
                    e.onError(ex);
                }
            }
        });
    }

    public Completable setNickName(String token, String nickName){
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                try {
                    Response<String> response = userService.setNickName(token, nickName).execute();
                    String content = response.body();
                    JSONObject jsonObject = new JSONObject(content);

                    if(jsonObject.getBoolean("err")) {
                        e.onError(new Exception(jsonObject.getString("msg")));
                        return;
                    }
                    e.onComplete();
                } catch (IOException|JSONException ex) {
                    e.onError(ex);
                }
            }
        });
    }

    @Override
    public Observable<List<String>> getMarkedTags(Long uid) {
        return Observable.create(new ObservableOnSubscribe<List<String>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<String>> e) throws Exception {
                try {
                    Response<String> response = userService.getUserMarkedTag(uid.toString()).execute();
                    String content = response.body();
                    JSONObject jsonObject = new JSONObject(content);

                    if(jsonObject.getBoolean("err")) {
                        e.onError(new Exception(jsonObject.getString("msg")));
                        return;
                    }
                    JSONArray jsonElements = jsonObject.getJSONArray("tags");
                    List<String> tmpList = new ArrayList<String>();
                    for(int i=0;i<jsonElements.length();++i){
                        tmpList.add(jsonElements.getString(i));
                    }
                    e.onNext(tmpList);
                } catch (IOException|JSONException ex) {
                    e.onError(ex);
                }
            }
        });
    }
    private static final String IMG = "multipart/form-data";
    private static final String TXT_PLAIN = "text/plain";

    @Override
    public Completable uploadHeadPic(String token, String absolutePath) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                File file = new File(absolutePath);
                RequestBody requestBody = RequestBody.create(MediaType.parse(IMG),
                        file);

                RequestBody tokenReq = RequestBody.create(MediaType.parse(TXT_PLAIN),
                        file);

                Response response = userService.uploadHeadPic(requestBody, tokenReq).execute();

                XLog.d(response.body());
                //TODO method not allowed
            }
        });
    }

    @Override
    public Completable signUp(String username, String password) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                try {
                    Response<String> response = userService.signUp(username, password, username).execute();
                    String content = response.body();
                    JSONObject jsonObject = new JSONObject(content);

                    if(jsonObject.getBoolean("err")) {
                        e.onError(new Exception(jsonObject.getString("msg")));
                        return;
                    }
                    e.onComplete();
                } catch (IOException|JSONException ex) {
                    e.onError(ex);
                }
            }
        });
    }

    @Override
    public Completable markPicTag(String token, String uuid, String tagName) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                try {
                    Response<String> response = userService.markPicTag(token, uuid, tagName).execute();
                    String content = response.body();
                    JSONObject jsonObject = new JSONObject(content);

                    if(jsonObject.getBoolean("err")) {
                        e.onError(new Exception(jsonObject.getString("msg")));
                        return;
                    }

                    e.onComplete();
                } catch (IOException|JSONException ex) {
                    e.onError(ex);
                }
            }
        });
    }

}