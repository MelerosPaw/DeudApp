package melerospaw.deudapp.iu.adapters;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnFocusChange;
import melerospaw.deudapp.R;
import melerospaw.deudapp.data.ContactManager;
import melerospaw.deudapp.modelo.Persona;
import melerospaw.deudapp.utils.StringUtils;

public class AdaptadorPersonasNuevas extends RecyclerView.Adapter<AdaptadorPersonasNuevas.ViewHolder> {

    private List<Persona> mDatos;
    private List<Persona> listaPersonas;
    private Context mContext;
    private AutocompleteAdapter adapter;
    private boolean focus;

    public AdaptadorPersonasNuevas(Context context, List<Persona> mDatos,
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

    public boolean hayNombresRepetidos() {
        int size = mDatos.size();
        for (int i = 0; i < size; i++) {
            for (int j = size - 1; j > i; j--) {
                if (mDatos.get(i).getNombre().equals(mDatos.get(j).getNombre())) {
                    return true;
                }
            }
        }
        return false;
    }




    /**VIEWHOLDER*/
    class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.actv_persona)    AutoCompleteTextView actvAcreedor;

        private Persona persona;

        private ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void bindView(Persona persona, Boolean focus){

            this.persona = persona;

            actvAcreedor.setText(persona.getNombre());
            filtrarLista();
            adapter = new AutocompleteAdapter(mContext, R.layout.item_autocomplete, listaPersonas);
            actvAcreedor.setAdapter(adapter);
            actvAcreedor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Persona personaSeleccionada = adapter.getItem(i);
                    ViewHolder.this.persona.setColor(personaSeleccionada.getColor());
                    ViewHolder.this.persona.setImagen(personaSeleccionada.getImagen());
                }
            });

            if (StringUtils.isCadenaVacia(persona.getNombre()) && focus != null && focus) {
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
                persona.setNombre(nombre);
            }
        }
    }

    public void filtrarLista() {
        ContactManager.eliminarRepetidos(listaPersonas);
        ContactManager.ordenar(listaPersonas);
    }
}

