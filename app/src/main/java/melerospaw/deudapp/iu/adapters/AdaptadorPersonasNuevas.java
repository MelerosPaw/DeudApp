package melerospaw.deudapp.iu.adapters;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnFocusChange;
import melerospaw.deudapp.R;
import melerospaw.deudapp.modelo.Persona;
import melerospaw.deudapp.utils.StringUtils;

public class AdaptadorPersonasNuevas extends RecyclerView.Adapter<AdaptadorPersonasNuevas.ViewHolder> {

    private List<Persona> mDatos;
    private List<Persona> listaPersonas;
    private AppCompatActivity mContext;
    private boolean focus;

    public AdaptadorPersonasNuevas(AppCompatActivity context, List<Persona> mDatos,
                                   List<Persona> personas) {
        this.mContext = context;
        this.mDatos = mDatos;
        this.listaPersonas = personas;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_nueva_persona, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Persona persona = mDatos.get(position);
        if (position == mDatos.size() - 1)
            holder.bindView(persona, focus);
        else
            holder.bindView(persona, null);
    }


    @Override
    public int getItemCount() {
        return mDatos.size();
    }


    /**Añade un nuevo hueco para persona a la lista*/
    public void nuevaPersona() {
        Persona persona = new Persona();
        mDatos.add(persona);
        notifyItemInserted(mDatos.size());
        focus = true;
    }


    /**Returns every Acreedor with name assigned in the RV*/
    public List<Persona> getPersonas() {
        List<Persona> personas = new ArrayList<>();

        for (Persona persona : mDatos){
            if (!persona.getNombre().trim().isEmpty())
                personas.add(persona);
        }

        return personas;
    }

    public boolean hayAlguien() {
        return !getPersonas().isEmpty();
    }




    /**VIEWHOLDER*/
    class ViewHolder extends RecyclerView.ViewHolder{

        @Bind(R.id.actv_persona)    AutoCompleteTextView actvAcreedor;

        private Persona persona;

        private ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void bindView(Persona persona, Boolean focus){

            this.persona = persona;

            actvAcreedor.setText(persona.getNombre());
            // TODO: 21/09/2017 Eliminar contactos que ya están como deudores y luego ordenar
            filtrarLista();
            // TODO: 19/09/2017 Mostrar foto
            AutocompleteAdapter adapter = new AutocompleteAdapter(mContext, R.layout.item_autocomplete, listaPersonas);
            actvAcreedor.setAdapter(adapter);

            if (StringUtils.isCadenaVacia(persona.getNombre()) && focus != null && focus){
                actvAcreedor.setVisibility(View.VISIBLE);
                actvAcreedor.requestFocus();
            }

            actvAcreedor.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        cerrarEdicion(false);
                    }
                    return false;
                }
            });
        }

        @OnFocusChange(R.id.actv_persona)
        public void cerrarEdicion(boolean hasFocus) {
            if (!hasFocus){
                String nombre = actvAcreedor.getText().toString();
                if (!StringUtils.isCadenaVacia(nombre)) {
                    persona.setNombre(nombre);
                }
            }
        }
    }

    public void filtrarLista() {
    }
}

