package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExtractedAudioEntity
import com.example.data.model.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoStudioDao {
    @Query("SELECT * FROM projects ORDER BY lastModified DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    fun getProjectById(id: Long): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectByIdDirect(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Query("SELECT * FROM extracted_audio ORDER BY createdAt DESC")
    fun getAllExtractedAudio(): Flow<List<ExtractedAudioEntity>>

    @Query("SELECT * FROM extracted_audio WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getExtractedAudioByProjectId(projectId: Long): Flow<List<ExtractedAudioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExtractedAudio(audio: ExtractedAudioEntity): Long

    @Delete
    suspend fun deleteExtractedAudio(audio: ExtractedAudioEntity)

    @Query("DELETE FROM extracted_audio WHERE id = :id")
    suspend fun deleteExtractedAudioById(id: Long)
}
