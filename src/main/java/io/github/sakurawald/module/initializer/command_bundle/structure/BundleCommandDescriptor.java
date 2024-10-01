package io.github.sakurawald.module.initializer.command_bundle.structure;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.core.command.structure.CommandDescriptor;
import io.github.sakurawald.core.command.structure.CommandRequirementDescriptor;
import io.github.sakurawald.core.service.command_executor.CommandExecutor;
import io.github.sakurawald.module.initializer.command_bundle.accessor.CommandContextAccessor;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BundleCommandDescriptor extends CommandDescriptor {

    @SuppressWarnings("RegExpRedundantEscape")
    private static final Pattern BUNDLE_COMMAND_DSL = Pattern.compile("([<](\\S+)\\s+(\\S+)[>])|(\\[(\\S+)\\s+(\\S+)\\s?([\\s\\S]*?)\\])|(\\S+)");
    private static final int LEXEME_GROUP_INDEX = 0;
    private static final int REQUIRED_NON_OPTIONAL_ARGUMENT_TYPE_GROUP_INDEX = 2;
    private static final int REQUIRED_NON_OPTIONAL_ARGUMENT_NAME_GROUP_INDEX = 3;
    private static final int REQUIRED_OPTIONAL_ARGUMENT_TYPE_GROUP_INDEX = 5;
    private static final int REQUIRED_OPTIONAL_ARGUMENT_NAME_GROUP_INDEX = 6;
    private static final int REQUIRED_OPTIONAL_ARGUMENT_DEFAULT_VALUE_GROUP_INDEX = 7;
    private static final int LITERAL_ARGUMENT_NAME_GROUP_INDEX = 8;
    private static final Map<String, Class<?>> str2type = new HashMap<>() {
        {
            this.put("int", int.class);
            this.put("str", String.class);
        }
    };
    private static final String ARGUMENT_NAME_PLACEHOLDER = "$";

    final BundleCommandEntry entry;
    @Getter
    final Map<String, String> defaultValueForOptionalArguments;

    private BundleCommandDescriptor(Method method, List<Argument> arguments, BundleCommandEntry entry, Map<String, String> defaultValueForOptionalArguments) {
        super(method, arguments);
        this.entry = entry;
        this.defaultValueForOptionalArguments = defaultValueForOptionalArguments;
    }

    @SneakyThrows
    private static Method getFunctionClosure() {
        Method functionClosure = BundleCommandDescriptor.class.getDeclaredMethod("executeBundleCommandClosure"
            , CommandContext.class
            , BundleCommandDescriptor.class
            , List.class);
        functionClosure.setAccessible(true);
        return functionClosure;
    }

    private static int executeBundleCommandClosure(
        @NotNull CommandContext<ServerCommandSource> ctx
        , @NotNull BundleCommandDescriptor descriptor
        , @NotNull List<Object> args) {

        /* log */
        LogUtil.debug("the closure for `bundle command` associated with {} is invoked with args: ", descriptor.entry);
        args.forEach(it -> LogUtil.debug("arg: {}", it));

        /* execute with context */
        List<String> commands = new ArrayList<>(descriptor.entry.getBundle());

        Map<String, String> variables = new HashMap<>();

        /* fill the variables */
        int argumentIndex = 0;
        for (Argument argument : descriptor.arguments) {
            if (argument.isLiteralArgument()) continue;

            String argumentName = argument.getArgumentName();
            String argumentValue = (String) args.get(argumentIndex);
            variables.put(argumentName, argumentValue);
            argumentIndex++;
        }
        LogUtil.debug("fill the variables with: {}", variables);

        /* substitute the variables */
        commands = commands.stream().map(command -> {
            String newCommand = command;
            for (Map.Entry<String, String> variable : variables.entrySet()) {
                String oldStr = ARGUMENT_NAME_PLACEHOLDER + variable.getKey();
                @NotNull String newStr = variable.getValue();
                newCommand = newCommand.replace(oldStr, newStr);
            }
            return newCommand;
        }).toList();

        /* execute the commands */
        LogUtil.debug("execute bundle command: {}", commands);
        CommandExecutor.executeSpecializedCommand(ctx.getSource().getPlayer(), commands);

        return 1;
    }

    public static BundleCommandDescriptor makeDynamicCommandDescriptor(BundleCommandEntry entry) {
        /* make arguments */
        List<Argument> arguments = new ArrayList<>();
        Map<String, String> defaultValueForOptionalArguments = new HashMap<>();

        String pattern = entry.getPattern();
        CommandRequirementDescriptor requirement = entry.getRequirement();

        Matcher matcher = BUNDLE_COMMAND_DSL.matcher(pattern);
        int argumentIndex = 0;
        while (matcher.find()) {

            if (matchLiteralArgument(matcher)) {
                String argumentName = matcher.group(LITERAL_ARGUMENT_NAME_GROUP_INDEX);
                arguments.add(Argument.makeLiteralArgument(argumentName, requirement));
            } else {
                boolean isOptional = matcher.group(LEXEME_GROUP_INDEX).startsWith("[");
                if (isOptional) {
                    String argumentType = matcher.group(REQUIRED_OPTIONAL_ARGUMENT_TYPE_GROUP_INDEX);
                    String argumentName = matcher.group(REQUIRED_OPTIONAL_ARGUMENT_NAME_GROUP_INDEX);
                    Class<?> type = getArgumentType(argumentType);
                    arguments.add(Argument.makeRequiredArgument(type, argumentName, argumentIndex, true, requirement));

                    // put default value for optional argument
                    String defaultValue = matcher.group(REQUIRED_OPTIONAL_ARGUMENT_DEFAULT_VALUE_GROUP_INDEX);
                    if (defaultValue == null) {
                        defaultValue = "";
                    }
                    defaultValueForOptionalArguments.put(argumentName, defaultValue);

                } else {
                    String argumentType = matcher.group(REQUIRED_NON_OPTIONAL_ARGUMENT_TYPE_GROUP_INDEX);
                    String argumentName = matcher.group(REQUIRED_NON_OPTIONAL_ARGUMENT_NAME_GROUP_INDEX);
                    Class<?> type = getArgumentType(argumentType);
                    arguments.add(Argument.makeRequiredArgument(type, argumentName, argumentIndex, false, requirement));
                }

            }

            argumentIndex++;
        }

        Method functionClosure = getFunctionClosure();
        return new BundleCommandDescriptor(functionClosure, arguments, entry, defaultValueForOptionalArguments);
    }

    private static boolean matchLiteralArgument(Matcher matcher) {
        return matcher.group(LITERAL_ARGUMENT_NAME_GROUP_INDEX) != null;
    }

    private static Class<?> getArgumentType(String typeString) {
        return str2type.get(typeString);
    }

    @Override
    protected List<Object> makeCommandFunctionArgs(CommandContext<ServerCommandSource> ctx) {
        List<Object> args = new ArrayList<>();

        CommandContextAccessor<?> ctxAccessor = (CommandContextAccessor<?>) ctx;
        for (Argument argument : this.arguments) {
            /* filter the literal command node and root command node. */
            if (!(argument.isRequiredArgument())) continue;

            String argumentName = argument.getArgumentName();
            ParsedArgument<?, ?> parsedArgument = ctxAccessor.fuji$getArguments().get(argumentName);

            /* collect the matched lexeme. */
            String arg = null;

            // for optional arg
            if (parsedArgument != null) {
                StringRange range = parsedArgument.getRange();
                arg = ctx.getInput().substring(range.getStart(), range.getEnd());
            } else {
                arg = getDefaultValueForOptionalArguments().get(argumentName);
            }

            args.add(arg);
        }
        LogUtil.debug("args for bundle command: {}", args);
        return args;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    protected Command<ServerCommandSource> makeCommandFunctionClosure() {
        return (ctx) -> {

            /* verify command source */
            if (!verifyCommandSource(ctx, this)) {
                return CommandHelper.Return.FAIL;
            }

            /* invoke the command function */
            List<Object> args = makeCommandFunctionArgs(ctx);
            BundleCommandDescriptor bundleCommandDescriptor = this;

            int value;
            try {
                value = (int) this.method.invoke(null, ctx, bundleCommandDescriptor, args);
            } catch (Exception e) {
                /* get the real exception during reflection. */
                Throwable theRealException = e;
                if (e instanceof InvocationTargetException) {
                    theRealException = e.getCause();
                }

                /* handle AbortCommandExecutionException */
                if (theRealException instanceof AbortCommandExecutionException) {
                    // the logging is done before throwing the AbortOperationException, here we just swallow this exception.
                    return CommandHelper.Return.FAIL;
                }

                /* report the exception */
                reportException(ctx.getSource(), this.method, theRealException);
                return CommandHelper.Return.FAIL;
            }

            return value;
        };
    }
}