package brave.dubbo;

import brave.Span;
import brave.Tracer;
import brave.propagation.CurrentTraceContext;
import brave.propagation.SamplingFlags;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;

public class DubboClientHandler {
  final Tracer tracer;
  final DubboClientParser parser;
  final DubboClientAdapter adapter;
  final CurrentTraceContext currentTraceContext;

  public static DubboClientHandler create(DubboTracing dubboTracing,
      DubboClientAdapter dubboClientAdapter) {
    return new DubboClientHandler(dubboTracing, dubboClientAdapter);
  }

  DubboClientHandler(DubboTracing dubboTracing, DubboClientAdapter adapter) {
    this.tracer = dubboTracing.tracing().tracer();
    this.parser = dubboTracing.clientParser();
    this.adapter = adapter;
    this.currentTraceContext = dubboTracing.tracing().currentTraceContext();
  }

  /**
   * Starts the client span after assigning it a name and tags.
   */
  public Span handleSend(TraceContext.Extractor extractor, TraceContext.Injector injector) {
    Span span = nextSpan(extractor.extract(RpcContext.getContext().getAttachments()));
    injector.inject(span.context(), RpcContext.getContext().getAttachments());
    if (span.isNoop()) return span;
    span.kind(Span.Kind.CLIENT);
    Tracer.SpanInScope ws = tracer.withSpanInScope(span);
    try {
      parser.request(adapter, RpcContext.getContext(), span);
    } finally {
      ws.close();
    }
    return span.start();
  }

  /**
   * Returns span from TraceContext or dubbo attachments.
   */
  public Span nextSpan(TraceContextOrSamplingFlags extracted) {
    TraceContext parent = currentTraceContext.get();
    //If spanInScope is closed we can use dubbo attachments
    if (parent == null) {
      parent = extracted.context();
    }
    if (parent != null) {
      return tracer.newChild(parent);
    }
    return tracer.newTrace(SamplingFlags.NOT_SAMPLED);
  }

  public void handleReceive(Result rpcResult, Span span) {
    if (span.isNoop()) return;
    Tracer.SpanInScope ws = tracer.withSpanInScope(span);
    try {
      parser.response(adapter, rpcResult, span);
    } finally {
      ws.close();
      span.finish();
    }
  }
}