package me.yamakaja.commanditems.interpreter;

import me.yamakaja.commanditems.CommandItems;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Yamakaja on 26.05.18.
 */
public class InterpretationContext {

    private static Lock cacheLock = new ReentrantLock();
    private static Deque<InterpretationStackFrame> stackFrameCache = new ArrayDeque<>();

    private Deque<InterpretationStackFrame> interpretationStack = new ArrayDeque<>();
    private CommandItems plugin;
    private Player player;

    public InterpretationContext(CommandItems plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public InterpretationContext(InterpretationContext context) {
        this.plugin = context.plugin;
        this.player = context.player;

        for (InterpretationStackFrame frame : context.interpretationStack)
            this.interpretationStack.add(frame.copy(getNewFrame()));
    }

    private static void addToCache(InterpretationStackFrame frame) {
        try {
            cacheLock.lock();
            stackFrameCache.push(frame);
        } finally {
            cacheLock.unlock();
        }
    }

    private static InterpretationStackFrame getFromCache() {
        try {
            cacheLock.lock();

            if (stackFrameCache.size() > 0)
                return stackFrameCache.pop();

            return new InterpretationStackFrame();
        } finally {
            cacheLock.unlock();
        }
    }

    public CommandItems getPlugin() {
        return plugin;
    }

    public Player getPlayer() {
        return player;
    }

    private InterpretationStackFrame getNewFrame() {
        if (stackFrameCache.size() < 1)
            return new InterpretationStackFrame();

        return stackFrameCache.remove();
    }

    public void pushFrame() {
        this.interpretationStack.addFirst(new InterpretationStackFrame());
    }

    public void popFrame() {
        InterpretationStackFrame stackFrame = this.interpretationStack.removeFirst();
        stackFrame.reset();
        stackFrameCache.push(stackFrame);
    }

    public void pushLocal(String key, String value) {
        this.interpretationStack.getFirst().pushLocal(key, value);
    }

    public String resolveLocal(String key) {
        Iterator<InterpretationStackFrame> iterator = this.interpretationStack.iterator();
        while (iterator.hasNext()) {
            InterpretationStackFrame next = iterator.next();
            String result = next.getLocal(key);
            if (result != null)
                return result;
        }

        return null;
    }

    public String resolveLocalsInString(String input) {
        char[] chars = input.toCharArray();
        StringBuilder outputBuilder = new StringBuilder();

        boolean escaped = false;

        for (int i = 0; i < chars.length; i++) {
            if (escaped) {
                outputBuilder.append(chars[i]);
                escaped = false;
                continue;
            } else if (chars[i] == '\\') {
                escaped = true;
                continue;
            }

            if (escaped || chars[i] != '{') {
                outputBuilder.append(chars[i]);
                continue;
            }

            int end = input.indexOf('}', i);
            if (end == -1)
                throw new RuntimeException("Unterminated curly braces!");

            String localName = input.substring(i + 1, end);
            String local = this.resolveLocal(localName);

            if (local == null)
                throw new RuntimeException("Attempt to access undefined local '" + localName + "'!");

            outputBuilder.append(local);
            i = end;
        }

        return outputBuilder.toString();
    }

    public InterpretationContext copy() {
        return new InterpretationContext(this);
    }

    @Override
    protected void finalize() {
        for (InterpretationStackFrame frame : this.interpretationStack) {
            frame.reset();
            addToCache(frame);
        }

        this.interpretationStack.clear();
    }
}
