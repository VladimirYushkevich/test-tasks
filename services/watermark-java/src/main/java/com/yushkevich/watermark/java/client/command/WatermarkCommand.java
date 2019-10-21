package com.yushkevich.watermark.java.client.command;

import com.yushkevich.watermark.java.client.WatermarkClient;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.List;

/**
 * Hystrix command.
 */

@Slf4j
@Getter
@Setter
public class WatermarkCommand extends BaseCommand<String> {

    private final List<String> watermarkProperties;
    private final WatermarkClient watermarkClient;

    @Builder
    public WatermarkCommand(String groupKey, int timeout, String debugMessage, List<String> watermarkProperties,
                            WatermarkClient watermarkClient) {
        super(groupKey, timeout, debugMessage);
        this.watermarkProperties = watermarkProperties;
        this.watermarkClient = watermarkClient;
    }

    /**
     * Executes time expensive call to {@link WatermarkClient} in separate thread.
     *
     * @return Observable of watermark property
     */
    @Override
    protected Observable<String> construct() {
        log.debug("::constructed observable for {}", watermarkProperties);
        return Observable.<String>create(subscriber -> {
                    try {
                        subscriber.onNext(watermarkClient.createWatermark(watermarkProperties));
                        subscriber.onCompleted();
                    } catch (Throwable ex) {
                        log.error("Failure get watermark for {}", watermarkProperties);
                        subscriber.onError(ex);
                    }
                }
        ).subscribeOn(Schedulers.computation());
    }

    @Override
    protected Observable<String> resumeWithFallback() {
        log.warn("::resumeWithFallback");
        return Observable.create(subscriber -> {
            try {
                handleErrors();
                subscriber.onNext("");
                subscriber.onCompleted();
            } catch (Exception ex) {
                log.error("Failure get watermark in fallback for {}", watermarkProperties);
                subscriber.onError(ex);
            }
        });
    }
}
