package com.example.test_app;
/***************************
 可以学习的内容：1.控件消息， 如单击
 2.控件与控件之间联动


 可用逻辑 ：按钮单击用来控制硬件
 文本框用来收集硬件上报的值：例如温度

 */

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.concurrent.ScheduledExecutorService;

public class MainActivity extends AppCompatActivity {
    /*******ID控件变量初始化***********/
    private Button ID_BUTTON_0;
    private ImageView ID_IMAGE_0;
    private TextView ID_TEXT_0;
    int IMAGE0_flag=0;
    /*******MQTT协议变量初始化********/
    private MqttClient client;
    private MqttConnectOptions options;
    private String host="tcp://122.112.229.64:1883";
    private String userName="test";
    private String passWord="test";
    private String mqtt_id = "565402462"; //定义成自己的QQ号  切记！不然会掉线！！！
    private String mqtt_sub_topic = "565402462"; //为了保证你不受到别人的消息  哈哈
    private String mqtt_pub_topic = "565402462_PC"; //为了保证你不受到别人的消息  哈哈  自己QQ好后面加 _PC
    private ScheduledExecutorService scheduler;
    private Handler handler;

    protected void onCreate(Bundle savedInstanceState) {
        //这里是界面打开后最先开始运行的地方
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//对应界面UI
        //一般先用来进行界面初始化 控件初始化 初始化一些参数变量

        ui_init();
        /**********BUTTON0单击方法*************/

        ID_BUTTON_0.setOnClickListener(new View.OnClickListener()
                                       {
                                           @Override
                                           public void onClick(View v)
                                           {
                                              //这里就是单击之后执行的地方
                                               //如toast弹窗
                                               Toast.makeText(MainActivity.this,"hello",Toast.LENGTH_SHORT).show(); //在当前activity显示内容为"hello"的短时间弹窗
                                           }
                                       }
        );

        /***********IMAGE0单击方法*********/

        ID_IMAGE_0.setOnClickListener(new View.OnClickListener()
                                      {

                                          @Override
                                          public void onClick(View v)
                                          {
                                              Toast.makeText(MainActivity.this,"this is IMAGE0",Toast.LENGTH_SHORT).show(); //在当前activity显示内容为"hello"的短时间弹窗
                                              ID_TEXT_0.setText("我是新的内容");

                                              if(IMAGE0_flag==0) {
                                                  ID_IMAGE_0.setColorFilter(0xFFFF0000);
                                                  IMAGE0_flag=1;
                                              }
                                              else{
                                                  ID_IMAGE_0.setColorFilter(0x00000000);
                                                  IMAGE0_flag=0;
                                                  }
                                          }
                                      }
                                    );



    }

    private void ui_init() {
        ID_BUTTON_0=findViewById(R.id.ID_BUTTON_0);//寻找xml中真正的id
        ID_IMAGE_0=findViewById(R.id.ID_IMAGE_0);//寻找xml中真正的id
        ID_TEXT_0=findViewById(R.id.ID_TEXT_0);//寻找xml中真正的id
    }

    private void mqtt_init(){
        try {
            //host 为主机名,androidtest123为cilentid,MemoryPersistence设置cilentid的保存形式(qos,retain播报等级使用)，默认以内存保存
            client = new MqttClient(host, mqtt_id,
                    new MemoryPersistence());
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(false);
            //设置连接的用户名
            options.setUserName(userName);
            //设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            //设置回调
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    System.out.println("connectionLost----------");
                    //startReconnect();
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                    System.out.println("deliveryComplete---------"
                            + token.isComplete());
                }
                @Override
                public void messageArrived(String topicName, MqttMessage message)
                        throws Exception {
                    //subscribe后得到的消息会执行到这里面
                    System.out.println("messageArrived----------");
                    Message msg = new Message();
                    msg.what = 3;   //收到消息标志位
                    msg.obj = topicName + "---" + message.toString();
                    handler.sendMessage(msg);    // hander 回传
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void Mqtt_connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(!(client.isConnected()) )  //如果还未连接
                    {
                        client.connect(options);
                        Message msg = new Message();
                        msg.what = 31;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = 30;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }
    private void startReconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!client.isConnected()) {
                    Mqtt_connect();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }
    private void publishmessageplus(String topic,String message2)
    {
        if (client == null || !client.isConnected()) {
            return;
        }
        MqttMessage message = new MqttMessage();
        message.setPayload(message2.getBytes());
        try {
            client.publish(topic,message);
        } catch (MqttException e) {

            e.printStackTrace();
        }
    }


