package br.com.convetec.githubsearch.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.convetec.githubsearch.domain.Repository
import br.com.convetec.githubsearch.R

class RepositoryAdapter(
    private val repositories: List<Repository>,
    private val onItemClick: (Repository) -> Unit,
    private val onShareClick: (Repository) -> Unit
) : RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

    // Cria uma nova view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.repository_item, parent, false)
        return ViewHolder(view)
    }

    // Pega o conteúdo da view e troca pela informação de item de uma lista
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val repository = repositories[position]
        repository.let {
            holder.repoName.text = it.name
            holder.itemView.setOnClickListener { onItemClick(repository) }
            holder.shareIcon.setOnClickListener {
                onShareClick(repository)
            }
        }
    }


    // Pega a quantidade de repositórios da lista
    override fun getItemCount(): Int = repositories.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val repoName: TextView = view.findViewById(R.id.tv_repository_name)
        val shareIcon: ImageView = view.findViewById(R.id.iv_compart)
    }
}



