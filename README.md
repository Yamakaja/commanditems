# Command Items

This plugin aims to create items that trigger actions. For more information see below.

## Compilation

* Clone the repo
* Depending on your system, run the correct gradle wrapper with the shadow task.
This would look like this under a Unix-like system: `./gradlew shadow`
* Get the artifact from `build/libs/CommandItems-<version>.jar`

## Usage

The configuration is based around so-called actions, which can trigger other actions
or perform certain tasks.

This is the default configuration file, which contains a couple examples that should
give you some insight into the functionality:

```yaml
items:
  fly:
    item:
      type: FEATHER
      name: "&a&lFlight token"
      lore: ["&1Click for 10 seconds of flight!", "&kRandom second line"]
      glow: true
    consumed: true
    cooldown: 20
    actions:
      - { action: COMMAND, by: CONSOLE, command: "fly {player} on" }
      - { action: MESSAGE, to: PLAYER, message: "&aFlight has been enabled!" }
      - { action: REPEAT, from: 9, to: 1, increment: -1, delay: 20, period: 20, actions: [{ action: MESSAGE, to: PLAYER, value: "&a{i}s to go" }]}
      - { action: WAIT, duration: 200, actions: [
          { action: COMMAND, by: CONSOLE, command: "fly {player} off" },
          { action: MESSAGE, to: PLAYER, message: "&cFlight has been disabled!" }
        ]}
  helpstick:
    item:
      type: STICK
      name: "&a&lHelp Stick"
      lore: ["&1Click to send a help request to moderators!"]
      glow: true
    consumed: false
    cooldown: 60
    actions:
      - { action: MESSAGE, to: PERMISSION, perm: group.moderator, message: "&6{player} &arequested help!" }
      - { action: MESSAGE, to: PLAYER, message: "&aModerators have been notified!" }
  xpparty:
    item:
      type: EXP_BOTTLE
      name: "&a&lEXP PARTY"
    consumed: true
    actions:
      - { action: REPEAT, period: 1, delay: 0, from: 0, to: 99, actions: [
          { action: ITER, what: ONLINE_PLAYERS, actions: [
            { action: CALC, a: "{iter_locY}", b: "4", op: ADD, target: "y", actions: [
              { action: COMMAND, by: CONSOLE, command: "minecraft:summon minecraft:xp_bottle {iter_locX} {y} {iter_locZ}" }]}]}]}
      - { action: MESSAGE, to: EVERYBODY, message: "&a&l{player} has started an XP party!" }

```

### Variables

Variables are provided by actions and can be used within strings in sub-actions.

Generally available are

| Variable | Description                                  |
|----------|----------------------------------------------|
| name     | The name of the player that clicked the item |
| uuid     | The UUID of the player that clicked the item |

### Available actions

#### COMMAND

This action can be used to execute commands.

| Parameter | Description                                                                   | Valid values                     | Default value | Required |
|-----------|-------------------------------------------------------------------------------|----------------------------------|---------------|----------|
| by        | Determines who the command is executed by                                     | PLAYER,CONSOLE,PLAYER_PRIVILEGED | PLAYER        | false    |
| command   | The command to execute                                                        | Any command, without slash       |               | true     |
| perm      | The permission that the player is temporarily given in PLAYER_PRIVILEGED mode | Any permission                   | *             | false    |

#### MESSAGE

This action can be used to send messages to players or the console:

| Parameter | Description                                                        | Valid values                        | Default value | Required              |
|-----------|--------------------------------------------------------------------|-------------------------------------|---------------|-----------------------|
| to        | The message mode, more info below                                  | PLAYER,CONSOLE,EVERYBODY,PERMISSION | PLAYER        | false                 |
| message   | The message to send                                                | Anything                            |               | true                  |
| perm      | In PERMISSION mode, the permission required to receive the message | Any permission                      | *             | if mode == PERMISSION |

| Mode       | Description                                                                                                 |
|------------|-------------------------------------------------------------------------------------------------------------|
| PLAYER     | The message is send to the player that clicked the item                                                     |
| CONSOLE    | The message is printed to the console, useful for logging purposes                                          |
| EVERYBODY  | The message is broadcasted to every player                                                                  |
| PERMISSION | The message is broadcasted to every player that has a certain permission, specified by the `perm` parameter |

#### CALC (Deprecated in favor of MATH_EXPR)

Perform some simple integer arithmetic

| Parameter | Description                                            | Valid values             | Default value | Required |
|-----------|--------------------------------------------------------|--------------------------|---------------|----------|
| op        | The operation to perform                               | ADD,SUB,MUL,DIV          |               | true     |
| a         | Operand a                                              | Integer                  |               | true     |
| b         | Operand b                                              | Integer                  |               | true     |
| target    | The variable which the result will be stored in        | Variable name            | y             | false    |
| actions   | Sub actions which can use the result of this operation | An array/list of actions |               | true     |

#### MATH_EXPR

Implements a small expression parser that can be used to evaluate mathematical expressions

| Parameter | Description                                            | Valid values             | Default value | Required |
|-----------|--------------------------------------------------------|--------------------------|---------------|----------|
| expr      | The operation to perform                               | <EXPR>                   |               | true     |
| round     | Round the output to int (1) or leave it as double (1.0)| boolean                  | false         | false    |
| target    | The variable which the result will be stored in        | Variable name            |               | true     |
| actions   | Sub actions which can use the result of this operation | An array/list of actions |               | true     |

The expression parser can deal with c-style arithmetic expressions, a couple examples:

* The gaussian pdf: `1/sqrt(2*pi*sigma^2)*exp(-(x-mu)^2/(2*sigma^2))`
* A gaussian random variable with mu = 5, sigma = 3: `randn()*3 + 5`
* A uniformly distributed random value between 10 and 20: `round(10 + 10 * rand())`

You can play with the expression parser using the `/cmdi calc` command, where the gaussian pdf can be evaluated for `sigma=5` and `mu=0` at `x=0` like this: `/cmdi calc 1/sqrt(2*pi*sigma^2)*exp(-(x-mu)^2/(2*sigma^2)) mu=0 sigma=5 x=0`.

**Note**: The `/cmdi calc` command cannot deal with spaces in the expression, because it uses spaces to delimit the expression of the context variables which are passed afterwards. This limitation is **NOT** present when specifying expressions in the command item configuration file.

Available variables:
* pi, e
* Numeric context variables

Available functions:

(Unless noted otherwise, trigonometric functions deal in radians, i.e. `sin(pi/2)` will evaluate to `1.0`.)

|   Function   | Description |
|--------------|-------------|
| `sqrt(x)`    |             |
| `sin(x)`     |             |
| `asin(x)`    | The inverse sin function |
| `cos(x)`     |             |
| `acos(x)`    | The inverse cos function |
| `tan(x)`     |             |
| `atan(x)`    | The inverse tan function |
| `ceil(x)`    | Always rounds up, i.e. `ceil(0.1)` -> `1`. |
| `floor(x)`   | Always rounds down |
| `abs(x)`     | returns `-x if x < 0 else x` |
| `exp(x)`     | `e^x` |
| `log(x)`     | Natural logarithm |
| `round(x)`   | Round to nearest integer |
| `min(x,y,...)` | Returns the smallest of n inputs |
| `max(x,y,...)` | Returns the greatest of n inputs |
| `fmod(x, y)` | `x % y` |
| `sign(x)`    | Returns the sign of `x`, or `0` for `x = 0` |
| `rand()`     | Uniform random number in `[0,1)` (`0` inclusive, `1` exclusive) |
| `randn()`    | Standard normal distributed variable |

Available operators are the usual:
* `+`, `-`, `*`, `/`
* `x^y`: `x` to the power `y`
* `x%y`: Return the remainder of x divided by y

#### ITER

Iterate over things

| Parameter | Description                                              | Valid values             | Default value | Required |
|-----------|----------------------------------------------------------|--------------------------|---------------|----------|
| what      | What to iterate over                                     | ONLINE_PLAYERS           |               | true     |
| perm      | A permission to filter players if what == ONLINE_PLAYERS | Any permission           |               | true     |
| actions   | Sub actions which can use the result of this operation   | An array/list of actions |               | true     |

##### ONLINE_PLAYERS

When iterating over online players, the following variables are defined:

| Variable         | Description                    |
|------------------|--------------------------------|
| iter_locX        | The x-coordinate of the player |
| iter_locY        | The y-coordinate of the player |
| iter_locZ        | The z-coordinate of the player |
| iter_name        | The name of the player         |
| iter_displayname | The display name of the player |
| iter_uuid        | The uuid of the player         |
| iter_health      | The health of the player       |
| iter_level       | The XP level of the player     |
| iter_food        | The food level of the player   |

#### REPEAT

The repeat action starts a timer that will repeatedly call its sub-actions until the iteration variable has reached its end.
This is similar to what a for loop does in Java, where i is the iteration variable: 

```
for (int i = start; increment > 0 ? i <= to : i >= to; i += increment)
    actions();
```

Available parameters are:

| Parameter  | Description                                                                       | Valid values             | Default value | Required |
|------------|-----------------------------------------------------------------------------------|--------------------------|---------------|----------|
| period     | The delay between two iterations in ticks                                         | Integer, >= 1            | 20            | false    |
| delay      | The delay before the first execution of the sub-actions                           | Integer, >= 0            | 20            | false    |
| from       | The iteration start variable, inclusive                                           | Integer                  | 0             | false    |
| to         | The iteration end variable, inclusive                                             | Integer                  | 9             | false    |
| increment  | The amount that the loop variable will be incremented with each iteration         | Integer                  | 1             | false    |
| counterVar | The variable through which the loop counter will be made available to sub-actions | Variable name            | i             | false    |
| actions    | Sub actions which will be executed with each loop cycle                           | An array/list of actions |               | true     |

The loop counter is available through the variable named by `counterVar`.

#### WAIT

Wait executes a set of tasks with a certain delay.

| Parameter | Description                                                 | Valid values             | Default value | Required |
|-----------|-------------------------------------------------------------|--------------------------|---------------|----------|
| duration  | The delay before the execution of the sub-actions, in ticks | Integer, >= 0            | 20            | false    |
| actions   | Sub actions which will be executed after the delay          | An array/list of actions |               | true     |
