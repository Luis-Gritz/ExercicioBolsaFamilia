package com.example.exerciciobolsafamilia;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {


    static String API = "http://www.transparencia.gov.br/api-de-dados/bolsa-familia-por-municipio";

    private TextView resultCidade, resultEstado, resultTotal, resultMedia, maiorValor, menorValor;
    private EditText editMunicipio, editAno;

    private Double valorTotal, valorMedia, valorMaior, valorMenor;
    private String nomeCidade, Estado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultCidade = findViewById(R.id.resultCidade);
        resultEstado = findViewById(R.id.resultEstado);
        resultTotal = findViewById(R.id.resultTotal);
        resultMedia = findViewById(R.id.resultMedia);
        maiorValor = findViewById(R.id.maiorValor);
        menorValor = findViewById(R.id.menorValor);

        editMunicipio = findViewById(R.id.editMunicipio);
        editAno = findViewById(R.id.editAno);
    }


    public void btnCarregarEvent(View v){
        carregarDados(v);
    }

    private void carregarDados(View view) {
        String codigoIbge = editMunicipio.getText().toString();
        valorTotal = 0.0;
        valorMedia = 0.0;
        valorMaior = -1.0;
        valorMenor = 99999999999.0;
        gerarRequestMesDoAno(codigoIbge);
    }


    private void gerarRequestMesDoAno(String codigoIbge) {
        String mes;
        for (int i = 1; i <= 12; i++) {
            if (i < 10) {
                mes = "0" + i;
            } else {
                mes = Integer.toString(i);
            }

            String dataConsulta = editAno.getText().toString() + mes;
            String endpoint = String.format(API+ "?mesAno=%s&codigoIbge=%s&pagina=1",dataConsulta, codigoIbge);

            gerarRequest(endpoint, 0);
        }
    }

    private void gerarRequest(String url, int operacao) {
        if (operacao == 0) {
            JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                JSONObject dataObject = response.getJSONObject(0);
                                extrairValoresDaResposta(dataObject);
                                inserirValoresNaTela();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) { }
            });

            APISingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

        } else if (operacao == 1) {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                editMunicipio.setText(response.get("id").toString());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) { }
            });

            APISingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        }
    }

    private void extrairValoresDaResposta(JSONObject dataObject) throws JSONException {
        valorTotal += Double.parseDouble(dataObject.getString("valor"));
        valorMedia += Double.parseDouble(dataObject.getString("quantidadeBeneficiados"));
        nomeCidade = dataObject.getJSONObject("municipio").getString("nomeIBGE");
        Estado = dataObject.getJSONObject("municipio").getJSONObject("uf").getString("nome");

        double valorMensal = Double.parseDouble(dataObject.getString("valor"));
        if(valorMaior <= valorMensal) {
            valorMaior = valorMensal;
        }
        if (valorMenor >= valorMensal) {
            valorMenor = valorMensal;
        }
    }

    private void inserirValoresNaTela() {
        resultCidade.setText("Nome da Cidade: " + nomeCidade);
        resultEstado.setText("Sigla do Estado: " + Estado);
        resultTotal.setText("Valor Total no Ano: R$" + String.format("%.2f", valorTotal));
        resultMedia.setText("Média dos beneficiados no ano: " + String.format("%.2f", valorMedia / 12));
        maiorValor.setText("Maior Valor no ano: R$" + String.format("%.2f", valorMaior));
        menorValor.setText("Menor Valor no ano: R$" + String.format("%.2f", valorMenor));
    }
}
