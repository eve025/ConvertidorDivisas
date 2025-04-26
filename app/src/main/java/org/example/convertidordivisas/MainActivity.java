package org.example.convertidordivisas;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String API_URL = "https://v6.exchangerate-api.com/v6/ef588c4effbccd4f64946a4a/latest/";
    private RequestQueue requestQueue;
    private EditText cantidadInput;
    private TextView resultadoText;
    private Spinner divisaOrigenSpinner, divisaDestinoSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        cantidadInput = findViewById(R.id.cantidadInput);
        resultadoText = findViewById(R.id.resultadoText);
        divisaOrigenSpinner = findViewById(R.id.divisaOrigenSpinner);
        divisaDestinoSpinner = findViewById(R.id.divisaDestinoSpinner);
        Button convertirBtn = findViewById(R.id.convertirBtn);

        // Configurar Volley
        requestQueue = Volley.newRequestQueue(this);

        // Configurar Spinners
        List<String> divisas = Arrays.asList("EUR", "USD", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "MXN");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, divisas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        divisaOrigenSpinner.setAdapter(adapter);
        divisaDestinoSpinner.setAdapter(adapter);
        divisaDestinoSpinner.setSelection(1); // USD por defecto

        // Configurar botón de conversión
        convertirBtn.setOnClickListener(view -> convertirDivisa());
    }

    private void convertirDivisa() {
        String cantidadStr = cantidadInput.getText().toString().trim();
        String divisaOrigen = divisaOrigenSpinner.getSelectedItem().toString();
        String divisaDestino = divisaDestinoSpinner.getSelectedItem().toString();

        if (cantidadStr.isEmpty()) {
            cantidadInput.setError("Ingrese una cantidad");
            return;
        }

        try {
            double cantidad = Double.parseDouble(cantidadStr);
            String url = API_URL + divisaOrigen;
            Log.d("CurrencyConverter", "URL: " + url);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    response -> {
                        try {
                            Log.d("CurrencyConverter", "Respuesta: " + response.toString());

                            if (response.getString("result").equals("success")) {
                                JSONObject rates = response.getJSONObject("conversion_rates");
                                double tasa = rates.getDouble(divisaDestino);
                                double resultado = cantidad * tasa;

                                // Actualizar UI en el hilo principal
                                runOnUiThread(() -> {
                                    resultadoText.setText(String.format(Locale.getDefault(), "%.2f %s = %.2f %s",
                                            cantidad, divisaOrigen, resultado, divisaDestino));
                                });
                            } else {
                                mostrarError("Error en la API: " + response.getString("result"));
                            }
                        } catch (JSONException e) {
                            mostrarError("Error procesando los datos");
                            Log.e("CurrencyConverter", "JSON Error: " + e.getMessage());
                        }
                    },
                    error -> {
                        mostrarError("Error de conexión: " + error.getMessage());
                        Log.e("CurrencyConverter", "Volley Error: " + error.toString());
                    }
            );

            requestQueue.add(request);
        } catch (NumberFormatException e) {
            cantidadInput.setError("Ingrese un número válido");
        }
    }

    private void mostrarError(String mensaje) {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
            resultadoText.setText("Error en la conversión");
        });
    }
}