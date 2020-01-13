package com.yusufcihan.aix.AsteroidDB; 

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.common.*;
import com.google.appinventor.components.runtime.util.YailList;

import org.json.*;
import java.util.HashMap;
import java.io.BufferedWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import android.util.Log;
import java.io.OutputStreamWriter;
import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;

import java.util.ArrayList;

@DesignerComponent(version = 1,
                   description = "AsteroidDB for App Inventor extension allows to connect your deployed AsteroidDB instance.<br>You can get yours from <a href=\"https://github.com/ysfchn/AsteroidDB\">https://github.com/ysfchn/AsteroidDB</a><br>- Yusuf Cihan",
                   category = ComponentCategory.EXTENSION,
                   nonVisible = true,
                   iconName = "https://yusufcihan.com/img/asteroiddb.png")
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "httpclient-4.4.1.2.jar,android-async-http-1.4.10,json.jar")
public class AsteroidDB extends AndroidNonvisibleComponent implements Component {

    private static String BASE_URL = "";
    private static String PASSWORD = "";

    public AsteroidDB(ComponentContainer container) {
        super(container.$form());
    }

    // ------------------------
    //        PROPERTIES
    // ------------------------

    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Specifies the URL of AsteroidDB. You need to have a deployed version of AsteroidDB in somewhere such as Heroku. For guide, check github.com/ysfchn/AsteroidDB")
    public String Url() {
      return BASE_URL;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty
    public void Url(String url) {
      if (url.endsWith("/")) {
        BASE_URL = url;
      }
      else
      {
        BASE_URL = url + "/";
      }
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Enter a password which will be used for all AsteroidDB queries. This doesn't change the password, this is used as password input. AsteroidDB, will ignore password input if your database is not locked with password.")
    public String Password() {
      return PASSWORD;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty
    public void Password(String password) {
      PASSWORD = password;
    }

    // ------------------------
    //        EVENTS
    // ------------------------

    @SimpleEvent(description = "This event called before request is started. You can use this event to display loading bars before connecting to the AsteroidDB.")
    public void OnStart() {
      EventDispatcher.dispatchEvent(this, "OnStart");
    }

    @SimpleEvent(description = "This event called when operation completed successfully. Use action parameter to get method name.")
    public void OnSuccess(String action, String result) {
      EventDispatcher.dispatchEvent(this, "OnSuccess", action, result);
    }

    @SimpleEvent(description = "This event called when HTTP status code is 4xx.")
    public void OnError(String message, int statusCode) {
      EventDispatcher.dispatchEvent(this, "OnError", message, statusCode);
    }

    /*
    @SimpleEvent(description = "This event called when request is retried.")
    public void OnRetry(int retryNo) {
      EventDispatcher.dispatchEvent(this, "OnRetry", retryNo);
    }
    */

    // ------------------------
    //       MAIN METHODS
    // ------------------------

    @SimpleFunction(description = "Store a value along with tag, or change the value of tag to database.")
    public void Store(String tag, String value) {
      RequestParams params = new RequestParams();
      params.put("pass", PASSWORD);
      params.put("tag", tag);
      params.put("value", value);
      POST("store",params);
    }

    @SimpleFunction(description = "Get the value from database with using tag.")
    public void GetTag(String tag) {
      RequestParams params = new RequestParams();
      params.put("pass", PASSWORD);
      params.put("tag", tag);
      POST("get",params);
    }

    @SimpleFunction(description = "Get all tags from database.")
    public void GetAll() {
      RequestParams params = new RequestParams();
      params.put("pass", PASSWORD);
      POST("getall",params);
    }

    @SimpleFunction(description = "Delete a record using tag.")
    public void Delete(String tag) {
      RequestParams params = new RequestParams();
      params.put("pass", PASSWORD);
      params.put("tag", tag);
      POST("delete",params);
    }

    @SimpleFunction(description = "Delete everything from database, including password protection. There is no way to recover them again! This method will be executed only when confirm parameter is set to true, for preventing unwanted operations.")
    public void Format(boolean confirm) {
      if (confirm) {
        RequestParams params = new RequestParams();
        params.put("pass", PASSWORD);
        POST("format",params);
      }
    }

    @SimpleFunction(description = "Change or set a password for database. Setting a password will require a password for every method.")
    public void SetPassword(String newpass) {
      RequestParams params = new RequestParams();
      params.put("oldpass", PASSWORD);
      params.put("newpass", newpass);
      POST("auth/password",params);
    }

    @SimpleFunction(description = "Removes the password protection. Using this method will don't require a password for every method.")
    public void Unlock() {
      RequestParams params = new RequestParams();
      params.put("pass", PASSWORD);
      POST("auth/unlock",params);
    }

    @SimpleFunction(description = "Get all data which stored in database as value and tag lists. This method doesn't include password record for security.")
    public void GetData() {
      RequestParams params = new RequestParams();
      params.put("pass", PASSWORD);
      POST("auth/data",params);
    }

    @SimpleFunction(description = "Returns true if database is locked with password. Otherwise, false.")
    public void IsLocked() {
      GET("islocked",null);
    }

    @SimpleFunction(description = "Returns a number that indicates how many records there are in database.")
    public void Count() {
      GET("count",null);
    }

    @SimpleFunction(description = "Returns the current AsteroidDB instance version.")
    public void Version() {
      GET("version",null);
    }

    @SimpleFunction(description = "Returns true if database password is equals with entered password. Otherwise, false. Can be useful in applications for checking the password before connecting to the database.")
    public void Test() {
      RequestParams params = new RequestParams();
      params.put("pass", PASSWORD);
      POST("istrue",params);
    }

    // ------------------------
    //      CUSTOM CALLS
    // ------------------------

    @SimpleFunction(description = "Calls AsteroidDB instance with custom parameters. This sends a POST request to your instance. Use this method to execute AsteroidDB methods which is not available in this extension yet. Use list of pairs for 'parameters' socket. Password parameter won't be added automatically.")
    public void CustomPost(String function, YailList parameters) 
    {
      RequestParams params = new RequestParams();
        for (int i = 0; i < parameters.size(); i++) 
        {
          params.put(getPairs(parameters, i, 0), getPairs(parameters, i, 0));
        }
        POST(function,params);
    }

    @SimpleFunction(description = "Calls AsteroidDB instance with custom parameters. This sends a GET request to your instance. Use this method to execute AsteroidDB methods which is not available in this extension yet. Use list of pairs for 'parameters' socket. Password parameter won't be added automatically.")
    public void CustomGet(String function, YailList parameters) 
    {
      RequestParams params = new RequestParams();
        for (int i = 0; i < parameters.size(); i++) 
        {
          params.put(getPairs(parameters, i, 0), getPairs(parameters, i, 0));
        }
        GET(function,params);
    }

    // ------------------------
    //      PARSE METHODS
    // ------------------------

    @SimpleFunction(description = "Returns the object in the JSON with key. Used to parse AsteroidDB responses. JSONArrays are automatically converted to App Inventor Lists.")
    public Object ParseResult(String result, String key)
    {
      try
      {
      JSONObject jb = new JSONObject(result);
      Object ob = new Object();

      // The variable which used for storing list of list values.
      ArrayList<YailList> responseArray = new ArrayList<YailList>();

      // The variable which used for storing String values.
      ArrayList<String> responseAltArray = new ArrayList<String>();

      boolean ISLISTOFLIST = false;

      // If user are trying to get the array from JSON,
      // convert it to YailList, to make it more compatible with App Inventor.
      if (jb.optJSONArray(key) != null) 
      {
        JSONArray ja = (JSONArray)(jb.get(key));
        // AsteroidDB returns list in list, so it is required to use for loop twice,
        // So it can read all values from JSONArray and add it to ArrayList. 
        // [['tag1','value1'],['tag2','value2']] 
        for (int i = 0; i < ja.length(); i++) 
        {
          JSONArray innerja = ja.optJSONArray(i);
          if (innerja != null) 
          {
            ISLISTOFLIST = true;
            ArrayList<String> pairs = new ArrayList<String>();
            for (int j = 0; j < innerja.length(); j++)
            {
              pairs.add(innerja.getString(j));
            }
            responseArray.add(YailList.makeList(pairs));
          }
          else
          {
            // If there is no JSONArray in the JSONArray, this means, there is no "list in list" situation.
            // This means object can be added to the result array directly.
            ISLISTOFLIST = false;
            responseAltArray.add(ja.getString(i));
          }
          
        }
        // Converts ArrayList to YailList.
        if (ISLISTOFLIST) {
          return YailList.makeList(responseArray);
        }
        else {
          return YailList.makeList(responseAltArray);
        }
      // If it is not a JSONArray, then just return the value by reading the JSON.
      }
      else
      {
        ob = jb.get(key);
        return ob;
      }
      }
      catch (JSONException h)
      {
      	return "";
      }
      
    }

    // ------------------------
    //     PRIVATE METHODS
    // ------------------------

    private String getPairs(YailList list, int i, int j)
    {
      return ((YailList)(list.getObject(i))).getString(j);
    }

    private void POST(String functionName, RequestParams param)
    {
        AsteroidDBRestClient.post(BASE_URL + functionName, param, new JsonHttpResponseHandler() {
          @Override
          public void onStart() {
            OnStart();
          }

          @Override
          public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            if (response.optString("action","NOT-FOUND") == "ERROR") {
              OnError(response.toString(), statusCode);
            }
            else 
            {
              OnSuccess(response.optString("action","NOT-FOUND"), response.toString());
            }
          }

          @Override
          public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
            //super.onFailure(statusCode, headers, errorResponse, e);
            OnError(errorResponse, statusCode);
          }

          @Override
          public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            try
            {
                OnError(errorResponse.toString(), statusCode);
            }
            catch (Exception e)
            {
                OnError(e.toString(), 0);
            }
          }
        });
    }

    private void GET(String functionName, RequestParams param)
    {
        AsteroidDBRestClient.get(BASE_URL + functionName, param, new JsonHttpResponseHandler() {
          @Override
          public void onStart() {
            OnStart();
          }

          @Override
          public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            if (response.optString("action","NOT-FOUND") == "ERROR") {
              OnError(response.toString(), statusCode);
            }
            else 
            {
              OnSuccess(response.optString("action","NOT-FOUND"),response.toString());
            }
          }

          @Override
          public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
                //super.onFailure(statusCode, headers, errorResponse, e);
            OnError(errorResponse, statusCode);
          }

          @Override
          public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            try
            {
                OnError(errorResponse.toString(), statusCode);
            }
            catch (Exception e)
            {
                OnError(e.toString(), 0);
            }
          }
        });
    }
    
}

class AsteroidDBRestClient {
  private static String BASE_URL = "";

  private static AsyncHttpClient client = new AsyncHttpClient();

  public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
      client.get(getAbsoluteUrl(url), params, responseHandler);
  }

  public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
      client.post(getAbsoluteUrl(url), params, responseHandler);
  }

  private static String getAbsoluteUrl(String relativeUrl) {
      return BASE_URL + relativeUrl;
  }

  private static void setURL(String url)
  {
      BASE_URL = url;
  }
}
