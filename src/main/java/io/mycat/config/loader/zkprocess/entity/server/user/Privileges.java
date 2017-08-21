package io.mycat.config.loader.zkprocess.entity.server.user;

import io.mycat.config.loader.zkprocess.entity.server.user.privilege.Schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * Created by huqing.yan on 2017/6/16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "privileges")
public class Privileges {
    @XmlAttribute(required = true)
    protected Boolean check;
    protected List<Schema> schema;

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public List<Schema> getSchema() {
        return schema;
    }

    public void setSchema(List<Schema> schema) {
        this.schema = schema;
    }

    @Override
    public String toString() {
        return "privileges{" + "check='" + check + '\'' + ", schema='" + schema + '}';
    }
}
