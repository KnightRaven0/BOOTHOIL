package liveitup.living;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class Living extends AppCompatActivity {

    //Variables for process
    boolean IsActive;			//Is Currently running
    boolean PlaySound;			//Should a ding play a sound
    boolean PlayVibration;		//Should a ding vibrate on appopriate platforms
    boolean PlayNotification;	//Should a ding show a notification

    long DingDelay;

    Calendar StartQuiet;
    Calendar EndQuiet;

    //Variables for updating
    long NextDingTime;	//Time the next ding will occur

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_living);
    }

    public void SetBeginSilentTime(View view) {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = 0;
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                StartQuiet = Calendar.getInstance();
                StartQuiet.set(Calendar.MINUTE, selectedMinute);
                StartQuiet.set(Calendar.HOUR, selectedHour + (((CheckBox)findViewById(R.id.checkBox4)).isChecked() ? 0 : 12));
                final TextView textViewToChange = (TextView) findViewById(R.id.SilentTimeBeginField);
                if (selectedMinute < 10){
                    textViewToChange.setText(selectedHour + ":0" + selectedMinute);
                }else {
                    textViewToChange.setText(selectedHour + ":" + selectedMinute);
                }
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }
    public void Update(){
        new Thread(new Runnable() {
            public void run() {
                for (; ; ) {
                    if (IsActive == false) {
                        return;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) findViewById(R.id.LastDingTime)).setText("ThisTime: " + System.currentTimeMillis());
                            ((TextView) findViewById(R.id.NextDingTime)).setText("Next Ding: " + NextDingTime);
                        }
                    });
                    if (System.currentTimeMillis() > NextDingTime) {
                        Ding();
                        Calendar mcurrentTime = Calendar.getInstance();
                        mcurrentTime.add(Calendar.MINUTE, (int)(DingDelay/60000));
                        if (mcurrentTime.before(StartQuiet)){
                            NextDingTime += DingDelay;
                        }else{
                            NextDingTime = EndQuiet.getTimeInMillis();
                        }
                    }
                }
            }
        }).start();
    }
    public void SetEndSilentTime(View view) {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = 0;
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                EndQuiet = Calendar.getInstance();
                EndQuiet.set(Calendar.MINUTE, selectedMinute);
                EndQuiet.set(Calendar.HOUR, selectedHour + (((CheckBox)findViewById(R.id.checkBox2)).isChecked() ? 0 : 12));
                final TextView textViewToChange = (TextView) findViewById(R.id.SilentTimeBeginField);
                if (selectedMinute < 10){
                    textViewToChange.setText(selectedHour + ":0" + selectedMinute);
                }else {
                    textViewToChange.setText(selectedHour + ":" + selectedMinute);
                }
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    public void ActionButton(View view){
        if (IsActive){
            ((Button)findViewById(R.id.button)).setText("Start");
            IsActive = false;
        }else{

            ((Button)findViewById(R.id.button)).setText("Stop");
            EditText temp = (EditText)findViewById(R.id.editText);
            DingDelay = Integer.valueOf((temp).getText().toString()) * 60000;
            NextDingTime = System.currentTimeMillis() + DingDelay;
            IsActive = true;
            PlayVibration = ((CheckBox)findViewById(R.id.radioButton)).isChecked();
            PlaySound = ((CheckBox)findViewById(R.id.radioButton2)).isChecked();
            PlayNotification = ((CheckBox)findViewById(R.id.radioButton3)).isChecked();
            Update();
        }
    }

    public void Ding(){
        if (PlayVibration) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(350);
        }
        if (PlayNotification){
            Integer temp = 0;
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher).setContentTitle("Infinite Loop Check").setContentText("It has been " + (DingDelay / 60000) + " minutes. ").setAutoCancel(true);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(temp, mBuilder.build());
        }
        if (PlaySound){
            MediaPlayer.create(this, R.raw.ding).start();
        }
    }
}
