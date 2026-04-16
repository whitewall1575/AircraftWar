package edu.hitsz.leaderboard.dao;

import edu.hitsz.leaderboard.model.ScoreRecord;

import java.util.List;

public interface ScoreRecordDao {
    List<ScoreRecord> getAll();

    void insert(ScoreRecord record);
}
