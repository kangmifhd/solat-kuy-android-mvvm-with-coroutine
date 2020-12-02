package com.programmergabut.solatkuy.ui.activityreadsurah

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.programmergabut.solatkuy.R
import com.programmergabut.solatkuy.base.BaseActivity
import com.programmergabut.solatkuy.data.local.localentity.MsFavAyah
import com.programmergabut.solatkuy.data.local.localentity.MsFavSurah
import com.programmergabut.solatkuy.data.remote.remoteentity.readsurahJsonAr.Ayah
import com.programmergabut.solatkuy.data.remote.remoteentity.readsurahJsonAr.Data
import com.programmergabut.solatkuy.util.enumclass.EnumConfig.Companion.LAST_READ_AYAH
import com.programmergabut.solatkuy.util.enumclass.EnumConfig.Companion.LAST_READ_SURAH
import com.programmergabut.solatkuy.util.enumclass.EnumStatus
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_read_surah.*
import kotlinx.android.synthetic.main.layout_read_surah.view.*


@AndroidEntryPoint
class ReadSurahActivity : BaseActivity(R.layout.activity_read_surah) {

    companion object{
        const val SURAH_ID = "SURAH_ID"
        const val SURAH_NAME = "SURAH_NAME"
        const val SURAH_TRANSLATION = "SURAH_TRANSLATION"
        const val IS_AUTO_SCROLL = "IS_AUTO_SCROLL"
    }

    private val viewModel: ReadSurahViewModel by viewModels()
    private lateinit var readSurahAdapter: ReadSurahAdapter
    private lateinit var mSelSurahId: String
    private lateinit var mSelSurahName: String
    private lateinit var mSelSurahTranslation: String
    private var mIsAutoScroll: Boolean = false
    private var mMenu: Menu? = null

    override fun setIntentExtra() {
        try{
            mSelSurahId = intent.getStringExtra(SURAH_ID) ?: throw Exception("getExtras surahID")
            mSelSurahName = intent.getStringExtra(SURAH_NAME) ?: throw Exception("getExtras surahName")
            mSelSurahTranslation = intent.getStringExtra(SURAH_TRANSLATION) ?: throw Exception("getExtras surahTranslation")
            mIsAutoScroll = intent.getBooleanExtra(IS_AUTO_SCROLL, false)
        }
        catch (ex: Exception){
            showBottomSheet(resources.getString(R.string.text_error_title), "", isCancelable = false, isFinish = true)
        }
    }
    override fun setFirstView() {
        cc_readQuran_loading.visibility = View.VISIBLE
        setSupportActionBar(tb_readSurah)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initRVReadSurah()
    }
    override fun setObserver() {
        super.setObserver()
        var data: Data? = null

        viewModel.selectedSurahAr.observe(this, {
            when (it.status) {
                EnumStatus.SUCCESS -> {
                    if (it.data == null)
                        showBottomSheet(isCancelable = false, isFinish = true)

                    data = it.data?.data!!
                    setVisibility(it.status)
                    setToolBarText(it.data.data)
                    viewModel.getListFavAyahBySurahID(mSelSurahId.toInt())
                }
                EnumStatus.LOADING -> {
                    setVisibility(it.status)
                    tb_readSurah.title = ""
                }
                EnumStatus.ERROR -> {
                    setVisibility(it.status)
                    lottieAnimationView.cancelAnimation()
                    tv_readQuran_loading.text = getString(R.string.fetch_failed)
                    showBottomSheet(isCancelable = false, isFinish = true)
                }
            }
        })

        viewModel.msFavAyahBySurahID.observe(this, { local ->

            val lastSurah = getLastReadSurah()
            val lastAyah = getLastReadAyah()

            if(lastSurah == -1 || lastAyah == -1){
                showBottomSheet("Error Occurred", "Surah and ayah not found",
                    isCancelable = true,
                    isFinish = true
                )
                return@observe
            }

            when (local.status) {
                EnumStatus.SUCCESS -> {
                    /* set the favorite ayah */
                    local.data?.forEach { ayah ->
                        data?.ayahs?.forEach out@{ remoteAyah ->
                            if (remoteAyah.numberInSurah == ayah.ayahID && mSelSurahId.toInt() == ayah.surahID) {
                                remoteAyah.isFav = true
                                return@out
                            }
                        }
                    }
                    /* set the last read ayah */
                    data?.ayahs?.forEach out@{
                        if (lastSurah == mSelSurahId.toInt() && lastAyah == it.numberInSurah) {
                            it.isLastRead = true
                            return@out
                        }
                    }

                    readSurahAdapter.apply {
                        listAyah = data?.ayahs!!
                        notifyDataSetChanged()
                    }
                    if(mIsAutoScroll){
                        val lastReadAyah = getLastReadAyah()
                        (rv_read_surah.layoutManager as LinearLayoutManager)
                            .scrollToPositionWithOffset(lastReadAyah - 1, 0)
                    }
                }
                EnumStatus.ERROR -> {
                    showBottomSheet(isCancelable = false, isFinish = true)
                }
                else -> {/*NO-OP*/}
            }
        })

        viewModel.fetchReadSurahAr(mSelSurahId.toInt())
    }

    private fun setToolBarText(data: Data) {
        tb_readSurah.title = data.englishName
        tb_readSurah.subtitle = data.revelationType + " - " + data.numberOfAyahs + " Ayahs"
    }

    private fun setVisibility(status: EnumStatus){
        when(status){
            EnumStatus.SUCCESS -> {
                rv_read_surah.visibility = View.VISIBLE
                ab_readQuran.visibility = View.VISIBLE
                cc_readQuran_loading.visibility = View.GONE
            }
            EnumStatus.LOADING -> {
                ab_readQuran.visibility = View.INVISIBLE
                cc_readQuran_loading.visibility = View.VISIBLE
                rv_read_surah.visibility = View.INVISIBLE
            }
            EnumStatus.ERROR -> {
                ab_readQuran.visibility = View.INVISIBLE
                rv_read_surah.visibility = View.INVISIBLE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.mMenu = menu
        menuInflater.inflate(R.menu.read_surah_menu, menu)

        viewModel.msFavSurah.observe(this, {
            when (it.status) {
                EnumStatus.SUCCESS -> {
                    if (it.data == null)
                        menu?.findItem(R.id.i_star_surah)?.icon =
                            ContextCompat.getDrawable(this, R.drawable.ic_star_24)
                    else
                        menu?.findItem(R.id.i_star_surah)?.icon =
                            ContextCompat.getDrawable(this, R.drawable.ic_star_purple_24)
                }
                EnumStatus.ERROR -> showBottomSheet(isCancelable = false, isFinish = true)
                else -> {/*NO-OP*/}
            }
        })

        viewModel.getFavSurahBySurahID(mSelSurahId.toInt())
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.i_star_surah -> {
                if (mMenu?.findItem(R.id.i_star_surah)?.icon?.constantState
                    == ContextCompat.getDrawable(this, R.drawable.ic_star_24)?.constantState)

                    viewModel.insertFavSurah(
                        MsFavSurah(
                            mSelSurahId.toInt(),
                            mSelSurahName,
                            mSelSurahTranslation
                        )
                    )
                else
                    viewModel.deleteFavSurah(
                        MsFavSurah(
                            mSelSurahId.toInt(),
                            mSelSurahName,
                            mSelSurahTranslation
                        )
                    )

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initRVReadSurah() {
        readSurahAdapter = ReadSurahAdapter(
            { data: Ayah, itemView: View ->
                val msFavAyah = MsFavAyah(
                    mSelSurahId.toInt(),
                    data.numberInSurah,
                    SURAH_NAME,
                    data.text,
                    data.textEn!!
                )
                if (itemView.iv_listFav_fav.drawable.constantState == ContextCompat.getDrawable(this, R.drawable.ic_favorite_red_24)?.constantState) {
                    Toasty.info(this, "Unsaving the ayah", Toast.LENGTH_SHORT).show()
                    viewModel.deleteFavAyah(msFavAyah)
                    itemView.iv_listFav_fav.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_favorite_24))
                } else {
                    Toasty.info(this, "Saving the ayah", Toast.LENGTH_SHORT).show()
                    viewModel.insertFavAyah(msFavAyah)
                    itemView.iv_listFav_fav.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_favorite_red_24))
                }
                viewModel.fetchReadSurahAr(mSelSurahId.toInt())
            },
            ContextCompat.getDrawable(this, R.drawable.ic_favorite_red_24)!!,
            ContextCompat.getDrawable(this, R.drawable.ic_favorite_24)!!,
            ContextCompat.getColor(this, R.color.purple_700),
            ContextCompat.getColor(this, R.color.dark_200)
        )

        rv_read_surah.apply {
            adapter = readSurahAdapter
            layoutManager = LinearLayoutManager(this@ReadSurahActivity)
            ItemTouchHelper(itemTouchCallback).attachToRecyclerView(this)
        }

        /* ItemClickSupport.addTo(rv_read_surah).setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
            override fun onItemClicked(recyclerView: RecyclerView?, position: Int, v: View?) {
                val test = ""
            }

            override fun onItemDoubleClicked(recyclerView: RecyclerView?, position: Int, v: View?) {
                val test = ""
            }
        }) */
    }

    private val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
        0, RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val pos = viewHolder.layoutPosition
            val item = readSurahAdapter.listAyah[pos]

            insertLastReadSharedPref(item.numberInSurah)
            Toasty.success(
                this@ReadSurahActivity,
                "Surah $mSelSurahId ayah ${item.numberInSurah} is now your last read",
                Toasty.LENGTH_LONG
            ).show()

            viewModel.fetchReadSurahAr(mSelSurahId.toInt())
            readSurahAdapter.notifyDataSetChanged()
        }

        override fun onChildDraw(
            canvas: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

            val background =  ColorDrawable(
                ContextCompat.getColor(
                    this@ReadSurahActivity,
                    R.color.dark_200
                )
            )
            background.setBounds(
                viewHolder.itemView.right, viewHolder.itemView.top,
                viewHolder.itemView.left, viewHolder.itemView.bottom
            )
            background.draw(canvas)

            /* val icon = ContextCompat.getDrawable(this@ReadSurahActivity, R.drawable.ic_check_white_24dp)
            icon?.setBounds(
                viewHolder.itemView.right, viewHolder.itemView.top,
                viewHolder.itemView.left, viewHolder.itemView.bottom
            )
            icon?.draw(canvas) */
        }
    }

    private fun insertLastReadSharedPref(numberInSurah: Int) {
        insertLastReadSharedPref(mSelSurahId.toInt(), numberInSurah)
    }

}