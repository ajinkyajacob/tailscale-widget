package com.ajinkya.tailscalewidget;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WidgetRendererStateTest {

    @Test
    public void notInstalled_winsOverOtherInputs() {
        assertEquals(WidgetState.NOT_INSTALLED, WidgetRenderer.resolveState(false, false, null));
        assertEquals(WidgetState.NOT_INSTALLED, WidgetRenderer.resolveState(false, true, null));
        assertEquals(WidgetState.NOT_INSTALLED, WidgetRenderer.resolveState(false, false, Boolean.TRUE));
        assertEquals(WidgetState.NOT_INSTALLED, WidgetRenderer.resolveState(false, true, Boolean.FALSE));
    }

    @Test
    public void pendingConnect_drivesTransitionStates() {
        assertEquals(WidgetState.CONNECTING,
                WidgetRenderer.resolveState(true, false, Boolean.TRUE));
        assertEquals(WidgetState.DISCONNECTING,
                WidgetRenderer.resolveState(true, true, Boolean.FALSE));
        // Pending overlay takes precedence over observed VPN state.
        assertEquals(WidgetState.CONNECTING,
                WidgetRenderer.resolveState(true, true, Boolean.TRUE));
        assertEquals(WidgetState.DISCONNECTING,
                WidgetRenderer.resolveState(true, false, Boolean.FALSE));
    }

    @Test
    public void observedVpnState_usedWhenNotPending() {
        assertEquals(WidgetState.CONNECTED, WidgetRenderer.resolveState(true, true, null));
        assertEquals(WidgetState.DISCONNECTED, WidgetRenderer.resolveState(true, false, null));
    }
}
