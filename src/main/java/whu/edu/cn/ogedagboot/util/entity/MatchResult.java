package whu.edu.cn.ogedagboot.util.entity;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchResult {

    private String modifiedStr;
    private List<String> matchList;

    public String getModifiedStr() {
        return modifiedStr;
    }

    public void setModifiedStr(String modifiedStr) {
        this.modifiedStr = modifiedStr;
    }

    public List<String> getMatchList() {
        return matchList;
    }

    public void setMatchList(List<String> matchList) {
        this.matchList = matchList;
    }
}
