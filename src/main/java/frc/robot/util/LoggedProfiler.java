package frc.robot.util;

import org.littletonrobotics.junction.Logger;

public class LoggedProfiler {
  public record LoggedProfilerConfig(LoggedProfilerLevel level, double delay, double regular) {}

  public static double kDelay = 3000.0;
  public static double kRegular = 2000.0;

  public enum LoggedProfilerLevel {
    None,
    Regular,
    Robot
  }

  public record LoggedProfilerLog(double max, double total, double free, double used) {

    public LoggedProfilerLog clipTo(double maxDelta) {
      var dFree = (this.free > maxDelta ? maxDelta : this.free);
      var dUsed = (this.used < maxDelta ? maxDelta : this.used);

      return new LoggedProfilerLog(this.max, this.total, dFree, dUsed);
    }

    public LoggedProfilerLog average(int n) {
      return new LoggedProfilerLog(0, 0, this.free / n, this.used / n);
    }
  }

  public record LoggedProfilerMem(long max, long total, long free, long used) {

    public LoggedProfilerMem delta(LoggedProfilerMem last) {
      return new LoggedProfilerMem(
          this.max - last.max,
          this.total - last.total,
          this.free - last.free,
          this.used - last.used);
    }

    // used memory is calculated, but must be tracked to be consistent
    public static LoggedProfilerLog scale(LoggedProfilerMem rec, double scale) {
      return new LoggedProfilerLog(
          rec.max * scale, rec.total * scale, rec.free * scale, rec.used * scale);
    }

    public static LoggedProfilerLog asMB(LoggedProfilerMem rec) {
      return scale(rec, Math.pow(2, -20));
    }

    public static LoggedProfilerLog asKB(LoggedProfilerMem rec) {
      return scale(rec, Math.pow(2, -10));
    }

    public static LoggedProfilerMem[] historyBuffer(int n) {
      LoggedProfilerMem[] buffer = new LoggedProfilerMem[kDeltaHistory];
      // this.deltaHistory[0] = new LoggedProfilerMem(1, 1, 1, 1);
      for (int i = 0; i < kDeltaHistory; i++) {
        buffer[i] = new LoggedProfilerMem(0, 0, 0, 0);
      }
      return buffer;
    }
  }

  private static LoggedProfiler instance;
  private static LoggedProfilerConfig configInstance;

  public static LoggedProfiler getOrCreate() {
    if (configInstance == null) {
      configInstance = new LoggedProfilerConfig(LoggedProfilerLevel.Regular, kDelay, kRegular);
    }

    if (instance == null) {
      instance = new LoggedProfiler(configInstance);
    }
    return instance;
  }

  public static void configure(LoggedProfilerConfig config) {
    if (configInstance == null) {
      configInstance = config;
    }

    if (instance == null) {
      instance = new LoggedProfiler(configInstance);
    }
  }

  private LoggedProfiler(LoggedProfilerConfig config) {
    this.config = config;

    // initialize circular buffer for history
    this.deltaHistory = LoggedProfilerMem.historyBuffer(kDeltaHistory);
  }

  public final LoggedProfilerConfig config;
  private LoggedProfilerMem current = new LoggedProfilerMem(0, 0, 0, 0);
  private LoggedProfilerMem prev = new LoggedProfilerMem(0, 0, 0, 0);
  private LoggedProfilerMem delta = new LoggedProfilerMem(0, 0, 0, 0);

  private int deltaCursor = 0;
  private static final int kDeltaHistory = 10;
  private final LoggedProfilerMem[] deltaHistory;
  private LoggedProfilerMem deltaSum = new LoggedProfilerMem(0, 0, 0, 0);

  // tracks the size of an outlier
  private boolean deltaClipping = false;
  private double deltaClipFree = 1024;
  private double deltaClipUsed = 1024;

  // to also log sum/average, since implementing the clipping was weird
  private static boolean kProfilerDebug = false;

  private double timeElapsed = 0.0;
  private double timeDt = 0.0;

  /** Set a baseline for this periodic cycle by recording */
  public static void record() {
    var p = LoggedProfiler.getOrCreate();
    p.measure();

    if (p.config.level != LoggedProfilerLevel.None) {
      Logger.recordOutput("LoggedProfiler/CurrentKB", LoggedProfilerMem.asKB(p.getCurrent()));
      Logger.recordOutput("LoggedProfiler/CurrentMB", LoggedProfilerMem.asMB(p.getCurrent()));

      var deltaKb = LoggedProfilerMem.asKB(p.getDelta());
      var deltaMb = LoggedProfilerMem.asMB(p.getDelta());

      if (p.deltaClipping) {
        deltaKb = deltaKb.clipTo((p.deltaClipFree + p.deltaClipUsed) / Math.pow(2, 10));
        deltaMb = deltaMb.clipTo((p.deltaClipFree + p.deltaClipUsed) / Math.pow(2, 20));
      }

      Logger.recordOutput("LoggedProfiler/DeltaKB", deltaKb);
      Logger.recordOutput("LoggedProfiler/DeltaMB", deltaMb);

      if (kProfilerDebug) {
        var deltaSum = p.getDeltaSum();
        Logger.recordOutput("LoggedProfiler/DeltaSum", deltaSum);

        // clipping the average still shows large java GC bumps. and its more incorrect
        var deltaKbAvg = LoggedProfilerMem.asKB(deltaSum).average(kDeltaHistory);
        var deltaMbAvg = LoggedProfilerMem.asMB(deltaSum).average(kDeltaHistory);
        Logger.recordOutput("LoggedProfiler/DeltaKBAvg", deltaKbAvg);
        Logger.recordOutput("LoggedProfiler/DeltaMBAvg", deltaMbAvg);
      }
    }
  }

  public LoggedProfilerMem getPrev() {
    return this.prev;
  }

  public LoggedProfilerMem getCurrent() {
    return this.current;
  }

  public LoggedProfilerMem getDelta() {
    return this.delta;
  }

  public void measure() {
    var elapsed = HALTime.readElapsed();
    var dt = elapsed - this.timeElapsed;

    if (shouldMeasure(elapsed, dt)) {
      // read profiler values
      Runtime rt = Runtime.getRuntime();
      long f = rt.freeMemory();
      long t = rt.totalMemory();
      long m = rt.maxMemory();

      // rotate profiler values
      this.prev = this.current;
      this.current = new LoggedProfilerMem(m, t, f, t - f);
      this.delta = this.current.delta(this.prev);

      this.historyRotate(this.delta);

      // update timestamps
      this.timeDt = dt;
      this.timeElapsed = elapsed;
    }
  }

  public boolean shouldMeasure(double elapsed, double dt) {
    return switch (config.level) {
      case None -> false;
      case Robot -> (elapsed > config.delay);
      case Regular -> (elapsed > config.delay && dt > config.regular);
    };
  }

  public LoggedProfilerMem getDeltaSum() {
    return this.deltaSum;
  }

  public void historyRotate(LoggedProfilerMem sample) {
    var oldSum = deltaSum;
    var oldRec = deltaHistory[deltaCursor];

    // update rotated sum (without iteration)
    var freeNewSum = deltaSum.free + (sample.free - oldRec.free);
    var usedNewSum = deltaSum.used + (sample.used - oldRec.used);
    deltaSum = new LoggedProfilerMem(0, 0, freeNewSum, usedNewSum);

    deltaClipFree = (deltaSum.free + oldRec.free) / (Math.sqrt((double) kDeltaHistory));
    deltaClipUsed = (deltaSum.used + oldRec.used) / (Math.sqrt((double) kDeltaHistory));

    // start clipping after one full rotation
    if (!deltaClipping && deltaCursor == kDeltaHistory - 1) {
      deltaClipping = true;
    }

    // rotate history
    deltaHistory[deltaCursor] = sample;
    deltaCursor = (deltaCursor + 1) % kDeltaHistory;
  }
}
