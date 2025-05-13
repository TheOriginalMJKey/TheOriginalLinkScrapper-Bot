package backend.academy.scrapper.handler;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.util.LinkSourceUtil;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public interface LinkUpdateHandler {

    String regex();

    Optional<LinkUpdate> getLinkUpdate(Link link);

    default MatchResult linkMatcher(Link link) {
        return Pattern.compile("https://" + LinkSourceUtil.getDomain(link.getLinkType()) + regex())
                .matcher(link.getUrl())
                .results()
                .toList()
                .getFirst();
    }
}
