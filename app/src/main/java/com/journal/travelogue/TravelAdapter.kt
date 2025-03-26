package com.journal.travelogue

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class TravelAdapter(private val travelList: MutableList<TravelItem>,
                    private val pageType: String ) :
    RecyclerView.Adapter<TravelAdapter.TravelViewHolder>() {

    class TravelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.card_view)
        val profileImage: ImageView = view.findViewById(R.id.profile)
        val userName: TextView = view.findViewById(R.id.user_name)
        val travelDescription: TextView = view.findViewById(R.id.travel_description)
        val placeImage: ImageView = view.findViewById(R.id.image)
        val placeName: TextView = view.findViewById(R.id.place_name)
        val likeIcon: ImageView = view.findViewById(R.id.like)
        val saveIcon: ImageView = view.findViewById(R.id.save)
        val editIcon: ImageView = view.findViewById(R.id.edit)
        val deleteIcon: ImageView = view.findViewById(R.id.delete)
        val locationIcon: ImageView = view.findViewById(R.id.location)
        val likeContainer: LinearLayout = view.findViewById(R.id.likeContainer)
        val savedContainer: LinearLayout = view.findViewById(R.id.savedContainer)
        val editContainer: LinearLayout = view.findViewById(R.id.editContainer)
        val deleteContainer: LinearLayout = view.findViewById(R.id.deleteContainer)
        val locationContainer: LinearLayout = view.findViewById(R.id.locationContainer)
        val likeText : TextView = view.findViewById(R.id.like_count)
        val saveText : TextView = view.findViewById(R.id.save_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.travel_card, parent, false)
        return TravelViewHolder(view)
    }

    override fun onBindViewHolder(holder: TravelViewHolder, position: Int) {
        val travelItem = travelList[position]

        holder.userName.text = travelItem.userName
        holder.profileImage.setImageResource(travelItem.profileImageRes)
        holder.travelDescription.text = travelItem.travelDescription
        holder.placeImage.setImageResource(travelItem.placeImageRes)
        holder.placeName.text = travelItem.placeName

        // Toggle Like & Save icons dynamically
        holder.likeIcon.setImageResource(if (travelItem.isLiked) R.drawable.liked else R.drawable.like)
        holder.saveIcon.setImageResource(if (travelItem.isSaved) R.drawable.saved else R.drawable.save)

        // Handle Like action
        holder.likeIcon.setOnClickListener {
            if (travelItem.isLiked) {
                travelItem.likeCount -= 1
            } else {
                travelItem.likeCount += 1
            }
            travelItem.isLiked = !travelItem.isLiked
            holder.likeIcon.setImageResource(if (travelItem.isLiked) R.drawable.liked else R.drawable.like)
            holder.likeText.text = travelItem.likeCount.toString() // Update text view
        }


        // Handle Save action
        holder.saveIcon.setOnClickListener {
            if (travelItem.isSaved) {
                travelItem.savedCount -= 1
            } else {
                travelItem.savedCount += 1
            }
            travelItem.isSaved = !travelItem.isSaved
            holder.saveIcon.setImageResource(if (travelItem.isSaved) R.drawable.saved else R.drawable.save)
            holder.saveText.text = travelItem.savedCount.toString() // Update text view
        }

        // Handle Edit action
        holder.editIcon.setOnClickListener {
            Toast.makeText(it.context, "Edit feature not implemented yet!", Toast.LENGTH_SHORT).show()
        }

        // Handle Delete action
        holder.deleteIcon.setOnClickListener {
            travelList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, travelList.size)
        }

        // Open Google Maps on Location click
        holder.locationIcon.setOnClickListener {
            val uri = "https://www.google.com/maps/search/?api=1&query=${travelItem.latitude},${travelItem.longitude}"
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            it.context.startActivity(mapIntent)
        }



        // Set visibility based on pageType
        if (pageType == "home" || pageType == "saved") {
            holder.locationContainer.visibility = View.VISIBLE
            holder.likeContainer.visibility = View.VISIBLE
            holder.savedContainer.visibility = View.VISIBLE

            holder.editContainer.visibility = View.GONE
            holder.deleteContainer.visibility = View.GONE
        } else if (pageType == "profile") {
            holder.locationContainer.visibility = View.VISIBLE
            holder.editContainer.visibility = View.VISIBLE
            holder.deleteContainer.visibility = View.VISIBLE

            holder.likeContainer.visibility = View.VISIBLE
            holder.savedContainer.visibility = View.VISIBLE
        }

    }


    override fun getItemCount(): Int = travelList.size
}
