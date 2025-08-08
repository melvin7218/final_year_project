package com.example.finalyearproject;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ingredient_auto_complete_adapter extends ArrayAdapter<String> implements Filterable {
   private List<String> usdaSuggestions;
   private Context context;

   public ingredient_auto_complete_adapter(Context context, int resource){
       super(context, resource);
       this.context = context;
       this.usdaSuggestions = new ArrayList<>();
   }

   @Override
    public Filter getFilter(){
       return new Filter() {
           @Override
           protected FilterResults performFiltering(CharSequence constraint) {
               FilterResults results = new FilterResults();
               if(constraint != null && constraint.length()>0){
                   fetchUSDASuggestions(constraint.toString(), suggestions ->{
                       usdaSuggestions = suggestions;
                       results.values = usdaSuggestions;
                       results.count = usdaSuggestions.size();
                       publishResults(constraint, results);
                   });
               }
               return  results;
           }

           @Override
           protected void publishResults(CharSequence constraint, FilterResults results) {
                    if(results != null  && results.count > 0){
                        clear();
                        addAll((List<String>) results.values);
                        notifyDataSetChanged();
                    }
           }
       };
   }

    private void fetchUSDASuggestions(String query, Consumer<List<String>> callback) {
        String url = "https://api.nal.usda.gov/fdc/v1/foods/search?api_key=Jsbh9n8n2xeQVd4f4nYPia1qAXMP7DLMQjJPSF83&query=" + URLEncoder.encode(query);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        List<String> suggestions = new ArrayList<>();
                        JSONArray foods = response.getJSONArray("foods");
                        for (int i = 0; i < Math.min(foods.length(), 5); i++) {
                            suggestions.add(foods.getJSONObject(i).getString("description"));
                        }
                        callback.accept(suggestions);
                    } catch (JSONException e) {
                        Log.e("USDA", "Error parsing suggestions", e);
                    }
                },
                error -> Log.e("USDA", "API error", error)
        );
        Volley.newRequestQueue(context).add(request);
    }
}
