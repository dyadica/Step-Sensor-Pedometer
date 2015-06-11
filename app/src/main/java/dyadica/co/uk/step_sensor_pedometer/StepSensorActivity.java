package dyadica.co.uk.step_sensor_pedometer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class StepSensorActivity extends Activity implements SensorEventListener
{
    private Context context;

    // Reference to the sensor manager

    private SensorManager sensorManager;

    // The sensors to be employed

    private Sensor senAccelerometer;
    private Sensor senStepCounter;
    private Sensor senStepDetector;

    // Properties to store step data

    private int stepsTaken = 0;
    private int reportedSteps = 0;
    private int stepDetector = 0;

    // GUI Components to display data

    private TextView countText;
    private TextView detectText;
    private TextView accelText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_sensor);

        // Reference the context

        context = this;

        // Check to see if the device has capability for step detection
        // If not show a toast to the user detailing so.

        if(!HasGotSensorCaps()){
            showToast("Required sensors not supported on this device!");
            return;
        }

        // Reference the GUI components

        detectText = (TextView)findViewById(R.id.StepDetect);
        countText = (TextView)findViewById(R.id.StepCount);
        accelText = (TextView)findViewById(R.id.AccelValues);

        // Reference/Assign the sensor manager

        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);

        // Reference/Assign the sensors

        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senStepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        senStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        // Register the sensors for event callback

        registerSensors();
    }


    // Method registered with the sensor manager which is called upon a sensor
    // update event, this is set via the registerSensors() method.
    @Override
    public void onSensorChanged(SensorEvent event)
    {
        // Get the sensor which has triggered the event

        Sensor sensor = event.sensor;

        // Perform differing functionality depending upon
        // the sensor type (caller)

        switch (event.sensor.getType())
        {
            case Sensor.TYPE_STEP_COUNTER:

                if (reportedSteps < 1){

                    // Log the initial value

                    reportedSteps = (int)event.values [0];
                }

                // Calculate steps taken based on
                // first value received.

                stepsTaken = (int)event.values [0] - reportedSteps;

                // Output the value to the simple GUI

                countText.setText("Cnt: " + stepsTaken);

                break;

            case Sensor.TYPE_STEP_DETECTOR:

                // Increment the step detector count

                stepDetector++;

                // Output the value to the simple GUI

                detectText.setText("Det: " + stepDetector);

                break;

            case  Sensor.TYPE_ACCELEROMETER:

                // Get the accelerometer values and set them to a string with 2dp

                String x = String.format("%.02f", event.values[0]);
                String y = String.format("%.02f", event.values[1]);
                String z = String.format("%.02f", event.values[2]);

                // Output the string to the GUI

                accelText.setText("Acc:" + x + "," + y + "," + z);

                break;
        }
    }

    // Method registered with the sensor manager which is called upon a sensor
    // accuracy update event, this is set via the registerSensors() method.
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onPause()
    {
        super.onPause();

        // Un-Register the sensors

        unRegisterSensors();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // Register the sensors

        registerSensors();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        // Un-Register the sensors

        unRegisterSensors();
    }

    // Method to check that the running device has the required capability to
    // perform step detection.
    public boolean HasGotSensorCaps()
    {
        PackageManager pm = context.getPackageManager();

        // Require at least Android KitKat

        int currentApiVersion = Build.VERSION.SDK_INT;

        // Check that the device supports the step counter and detector sensors

        return currentApiVersion >= 19
                && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
                && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR)
                && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
    }

    // Simple function that registers all of the required sensor listeners
    // with the sensor manager.
    private void registerSensors()
    {
        // Double check that the device has the required sensor capabilities

        if(!HasGotSensorCaps()){
            showToast("Required sensors not supported on this device!");
            return;
        }

        // Provide a little feedback via a toast

        showToast("Registering sensors!");

        // Register the listeners. Used for receiving notifications from
        // the SensorManager when sensor values have changed.

        sensorManager.registerListener(this, senStepCounter, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, senStepDetector, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Simple function that un-registers all of the required sensor listeners
    // from the sensor manager.
    private void unRegisterSensors()
    {
        // Double check that the device has the required sensor capabilities
        // If not then we can simply return as nothing will have been already
        // registered
        if(!HasGotSensorCaps()){
            return;
        }

        // Perform un-registration of the sensor listeners

        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }

    // Simple function that can be used to display toasts
    public void showToast(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}