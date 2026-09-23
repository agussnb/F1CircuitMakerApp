package com.example.circuitmakerf1.data
import com.example.circuitmakerf1.dao.CircuitDao
import com.example.circuitmakerf1.model.Circuit
import kotlinx.coroutines.flow.Flow

class CircuitRepository(private val circuitDao : CircuitDao) {
    val allCircuits: Flow<List<Circuit>> = circuitDao.getAll()

    suspend fun insertsCircuit(circuit : Circuit){
        circuitDao.insert(circuit)
    }

    suspend fun deleteCircuit(circuit : Circuit){
        circuitDao.delete(circuit)
    }

    suspend fun getCircuitById(id : Int) : Circuit?{
        return circuitDao.getById(id)
    }
    suspend fun getCircuitByName(name : String) : Circuit?{
        return circuitDao.getByName(name)
    }
}


