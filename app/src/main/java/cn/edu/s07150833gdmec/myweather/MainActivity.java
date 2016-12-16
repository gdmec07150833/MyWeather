package cn.edu.s07150833gdmec.myweather;

import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MainActivity extends AppCompatActivity {
    private MainActivity seft;
    //保存省份和城市，key为省份，value为其下级市
    private Map<String,List<String>> cityMap;

    private Spinner province_spinner;

    private Spinner city_spinner;

    private AlertDialog choose_dialog;

    private LinearLayout choose_layout;

    private ImageButton settingBtn;

    private ImageButton refreshBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seft=this;
        setContentView(R.layout.activity_main);
        //初始化省，市map;
        initProvinces();
        //初始化选择城市窗口
        initChooseDialog();
        settingBtn= (ImageButton) this.findViewById(R.id.setting);
        refreshBtn= (ImageButton) this.findViewById(R.id.refresh);
        //为设置天气按钮添加事件监听器
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choose_dialog.show();
            }
        });
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName=((TextView)seft.findViewById(R.id.city)).getText().toString();

                new GetWeatherInfoTask(seft).execute(cityName);
            }
        });
        new GetWeatherInfoTask(this).execute("广州");
    }

    private void initChooseDialog() {
        //创建一个警告对话框
        choose_dialog=new AlertDialog.Builder(seft).setTitle("选择城市")
                .setPositiveButton("确定",new ChooseCityListenner())
                .setNegativeButton("取消",null).create();
        LayoutInflater inflater=LayoutInflater.from(this);

        choose_layout= (LinearLayout) inflater.inflate(R.layout.choose,null);

        province_spinner= (Spinner) choose_layout.findViewById(R.id.province_spinner);

        ArrayAdapter<String> provinceAdapter=new ArrayAdapter<String>(this,
                R.layout.simple_list_item,new ArrayList<String>(
                cityMap.keySet()));
        province_spinner.setAdapter(provinceAdapter);

        city_spinner= (Spinner) choose_layout.findViewById(R.id.city_spinner);

        province_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String province_name=province_spinner.getSelectedItem().toString();

                ArrayAdapter<String> cityAdapter=new ArrayAdapter<String>(
                        seft,R.layout.simple_list_item,cityMap.get(province_name));
                city_spinner.setAdapter(cityAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //设置窗口的内容
        choose_dialog.setView(choose_layout);

    }
    private class ChooseCityListenner implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface dialog, int which) {
            //获取选中的城市
            String city_name=city_spinner.getSelectedItem().toString();
            TextView cityName= (TextView) seft.findViewById(R.id.city);
            //设置当前城市名称
            cityName.setText(city_name);
            //连接网络选取选中城市的天气
            new GetWeatherInfoTask(seft).execute(city_name);
        }
    }

    private void initProvinces() {
        AssetManager assetManager=getAssets();
        SaxHandler handler=new SaxHandler();
        InputStream inputStream=null;
        try {
            inputStream=assetManager.open("City.xml");
            SAXParserFactory factory=SAXParserFactory.newInstance();
            SAXParser parser=factory.newSAXParser();

            parser.parse(inputStream,handler);

            cityMap=handler.getCityMap();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }finally {
            if (inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}