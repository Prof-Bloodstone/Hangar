package io.papermc.hangar.db.dao;

import io.papermc.hangar.db.model.ProjectChannelsTable;
import io.papermc.hangar.service.project.ChannelService;
import io.papermc.hangar.model.Color;

import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterBeanMapper(ProjectChannelsTable.class)
public interface ProjectChannelDao {

    @SqlUpdate("insert into project_channels (created_at, name, color, project_id) values (:now, :name, :color, :projectId)")
    @Timestamped
    @GetGeneratedKeys
    ProjectChannelsTable insert(@BindBean ProjectChannelsTable projectChannel);

    @SqlUpdate("UPDATE project_channels SET name = :name, color = :color WHERE project_id = :projectId AND name = :oldName")
    void update(long projectId, String oldName, String name, @EnumByOrdinal Color color);

    @SqlQuery("SELECT * FROM project_channels WHERE project_id = :projectId LIMIT 1")
    ProjectChannelsTable getFirstChannel(long projectId);

    @SqlQuery("SELECT * FROM project_channels WHERE project_id = :projectId")
    List<ProjectChannelsTable> getProjectChannels(long projectId);

    @UseStringTemplateEngine
    @SqlQuery("SELECT " +
            "CASE WHEN countq.count > <maxChannels> THEN 'MAX_CHANNELS' " +
            "     WHEN '<channelName>' IN (SELECT \"name\" FROM project_channels WHERE project_id = :projectId) THEN 'DUPLICATE_NAME' " +
            "     WHEN <colorValue> IN (SELECT color FROM project_channels WHERE project_id = :projectId) THEN 'UNIQUE_COLOR' " +
            "END " +
            "FROM (SELECT COUNT(*) count FROM project_channels WHERE project_id = :projectId) AS countq")
    ChannelService.InvalidChannelCreationReason validateChannelCreation(long projectId, @Define String channelName, @Define long colorValue, @Define int maxChannels);

    @UseStringTemplateEngine
    @SqlQuery("SELECT " +
            "CASE WHEN '<channelName>' IN (SELECT \"name\" FROM project_channels WHERE project_id = :projectId AND name != :oldChannelName) THEN 'DUPLICATE_NAME' " +
            "     WHEN <colorValue> IN (SELECT color FROM project_channels WHERE project_id = :projectId AND name != :oldChannelName) THEN 'UNIQUE_COLOR' " +
            "END")
    ChannelService.InvalidChannelCreationReason validateChanneUpdate(long projectId, String oldChannelName, @Define String channelName, @Define long colorValue);

    @SqlQuery("SELECT * FROM project_channels WHERE project_id = :projectId AND (name = :channelName OR id = :channelId)")
    ProjectChannelsTable getProjectChannel(long projectId, String channelName, Long channelId);
}
